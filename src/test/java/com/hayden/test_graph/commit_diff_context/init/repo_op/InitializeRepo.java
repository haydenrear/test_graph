package com.hayden.test_graph.commit_diff_context.init.repo_op;

import com.hayden.commitdiffmodel.codegen.types.GitOperation;
import com.hayden.commitdiffmodel.codegen.types.RagOptions;
import com.hayden.commitdiffmodel.codegen.types.SessionKey;
import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoInitItem;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.commit_diff_context.service.CallGraphQlQueryArgs;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.utilitymodule.git.RepoUtil;
import com.hayden.utilitymodule.io.ArchiveUtils;
import org.assertj.core.util.Files;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class InitializeRepo implements RepoOpInitNode {

    @Autowired
    CommitDiff commitDiff;
    @Autowired
    Assertions assertions;


    @Override
    @Idempotent
    public RepoOpInit exec(RepoOpInit c, MetaCtx h) {
        // clone repo, add to context
        return c.repoData().res()
                .filterResult(rd -> rd.branchName() != null && rd.url() != null)
                .map(rd -> doPerformRepoInitializations(c, rd))
                .one()
                .orElseRes(c);
    }

    /**
     * There are many steps that use this, each one building the context object - @Then is called and it executes for all of them,
     * and executes other things it depends on, dynamically determined at runtime.
     * So... the graph IS used.
     * @param c
     * @param rd
     * @return
     */
    private @NotNull RepoOpInit doPerformRepoInitializations(RepoOpInit c, RepoOpInit.RepositoryData rd) {
        cloneIfRemote(c, rd);
        decompressIfArchive(c, rd);

        if (c.getRepoInitializations().simultaneously().optional().orElse(false)) {
            doSimultaneous(c);
        } else {
            doSerially(c);
        }

        return c;
    }

    private void doSerially(RepoOpInit c) {
        c.getRepoInitializations().initItems()
                .stream().sorted(RepoInitItem.c)
                .forEach(repoInit -> {
                    assertions.reportAssert("Executing repo init task: %s".formatted(repoInit.getClass().getName()));
                    switch (repoInit) {
                        case RepoInitItem.AddCodeBranch ignored ->
                                doAddGitOp(c, GitOperation.ADD_BRANCH);
                        case RepoInitItem.AddEmbeddings() ->
                                doAddGitOp(c, GitOperation.SET_EMBEDDINGS);
                        case RepoInitItem.AddBlameNodes ignored ->
                                doAddGitOp(c, GitOperation.PARSE_BLAME_TREE);
                        case RepoInitItem.UpdateHeadNode updateHeadNode ->
                                doAddGitOp(c, GitOperation.UPDATE_HEAD, updateHeadNode.ctx());
                    }
                });
    }

    private void doSimultaneous(RepoOpInit c) {
        var ops = c.getRepoInitializations().initItems().stream()
                .sorted(RepoInitItem.c)
                .map(RepoInitItem::op)
                .toList();
        var ctx = c.getRepoInitializations().initItems().stream()
                .sorted(RepoInitItem.c)
                .map(RepoInitItem::ctx)
                .toArray(Object[]::new);
        doAddGitOp(c, ops, ctx);
    }

    private void cloneIfRemote(RepoOpInit c, RepoOpInit.RepositoryData rd) {
        RepoUtil.cloneIfRemote(rd.url(), rd.branchName())
                .doOnError(repoUtilError -> assertions.assertSoftly(false, "Failed to clone git repo: %s.".formatted(repoUtilError.getMessage())))
                .ifPresent(path -> c.repoData().swap(rd.withClonedUri(path)));
    }

    private void decompressIfArchive(RepoOpInit c, RepoOpInit.RepositoryData rd) {
        RepoUtil.decompressIfArchive(rd.url())
                .doOnError(repoUtilError -> assertions.assertSoftly(false, "Failed to decompress git repo: %s.".formatted(repoUtilError.getMessage())))
                .ifPresent(path -> c.repoData().swap(rd.unzipped(path)));
    }

    private void doAddGitOp(RepoOpInit c, List<GitOperation> gitOperation) {
        doAddGitOp(c, gitOperation, (Object) null);
    }

    private void doAddGitOp(RepoOpInit c, GitOperation gitOperation) {
        doAddGitOp(c, List.of(gitOperation), (Object) null);
    }

    private void doAddGitOp(RepoOpInit c, GitOperation gitOp, Object ... ctx) {
        doAddGitOp(c, List.of(gitOp), ctx);
    }

    private void doAddGitOp(RepoOpInit c, List<GitOperation> gitOp, Object ... ctx) {
        var key = c.retrieveSessionKey();
        CallGraphQlQueryArgs.DoGitArgs addCodeBranchArgs = CallGraphQlQueryArgs.DoGitArgs.builder()
                .gitRepoPath(c.repoDataOrThrow().url())
                .branchName(c.repoDataOrThrow().branchName())
                .ctx(ctx)
                .sessionKey(key)
                .gitOperation(gitOp)
                .build();

        var added = commitDiff.callGraphQlQuery(addCodeBranchArgs);
        assertions.assertSoftly(added.isOk(),
                "Could not perform git operation %s. Error message: %s.".formatted(gitOp, added.errorMessage()),
                () -> "Git operation %s performed successfully. Response: %s".formatted(gitOp, added.r().get()));

        added.ifPresent(gitRepoResult -> c.setSessionKey(new SessionKey(gitRepoResult.getSessionKey().getKey())));
    }

}
