package com.hayden.test_graph.commit_diff_context.init.repo_op;

import com.hayden.commitdiffmodel.codegen.types.GitOperation;
import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.commit_diff_context.service.CallGraphQlQueryArgs;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.utilitymodule.io.ArchiveUtils;
import org.assertj.core.util.Files;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

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

    private @NotNull RepoOpInit doPerformRepoInitializations(RepoOpInit c, RepoOpInit.RepositoryData rd) {
        if (rd.url().endsWith(".tar")) {
            assertions.assertSoftly(new File(rd.url()).exists(), "Repo archive did not exist.");
            var tempDir = Files.newTemporaryFolder();
            var unzipped = ArchiveUtils.prepareTestRepos(Paths.get(rd.url()).getParent(), tempDir.toPath(), Paths.get(rd.url()).getFileName().toString());
            var unzippedTo = rd.unzipped(tempDir.toPath());
            c.repoData().swap(unzippedTo);
            assertions.assertSoftly(unzipped.isOk(), "Was unsuccessful in unzipping repositories.");
        }

        c.getRepoInitializations()
                .initItems()
                .stream()
                .sorted(RepoOpInit.RepoInitItem.c)
                .forEach(repoInit -> {
                    assertions.reportAssert("Executing repo init task: %s".formatted(repoInit.getClass().getName()));
                    switch (repoInit) {
                        case RepoOpInit.RepoInitItem.AddCodeBranch ignored ->
                                doAddGitOp(c, GitOperation.ADD_BRANCH);
                        case RepoOpInit.RepoInitItem.AddEmbeddings ignored ->
                                doAddGitOp(c, GitOperation.SET_EMBEDDINGS);
                        case RepoOpInit.RepoInitItem.AddBlameNodes ignored ->
                                doAddGitOp(c, GitOperation.PARSE_BLAME_TREE);
                        case RepoOpInit.RepoInitItem.UpdateHeadNode updateHeadNode ->
                                doAddGitOp(c, GitOperation.UPDATE_HEAD, updateHeadNode.ctx());
                    }
                });

        return c;
    }

    private void doAddGitOp(RepoOpInit c, GitOperation gitOperation) {
        doAddGitOp(c, gitOperation, null);
    }

    private void doAddGitOp(RepoOpInit c, GitOperation gitOp, Object ctx) {
        var key = c.retrieveSessionKey();
        CallGraphQlQueryArgs.DoGitArgs addCodeBranchArgs = CallGraphQlQueryArgs.DoGitArgs.builder()
                .gitRepoPath(c.repoDataOrThrow().url())
                .branchName(c.repoDataOrThrow().branchName())
                .ctx(ctx)
                .sessionKey(key)
                .gitOperation(gitOp)
                .build();

        var added = commitDiff.callGraphQlQuery(addCodeBranchArgs);

        assertions.assertSoftly(added.isOk(), "Could not add %s.".formatted(gitOp), "%s branch successfully".formatted(gitOp));
        added.e()
                .filter(cde -> Optional.ofNullable(cde.errors()).map(l -> !l.isEmpty()).orElse(false))
                .ifPresent(err -> assertions.assertSoftly(false, "Error on add %s: %s"
                    .formatted(gitOp, added.e().get().getMessage()), "%s completed successfully.".formatted(gitOp)));
    }

    @Override
    public Class<RepoOpInit> clzz() {
        return RepoOpInit.class;
    }

}
