package com.hayden.test_graph.commit_diff_context.init.repo_op;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.CommitDiffInitNode;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
                .map(rd -> doPerformRepoInitializations(c))
                .one()
                .orElseRes(c);
    }

    private @NotNull RepoOpInit doPerformRepoInitializations(RepoOpInit c) {
        c.getRepoInitializations()
                .initItems()
                .stream()
                .sorted(RepoOpInit.RepoInitItem.c)
                .forEach(repoInit -> {
                    switch (repoInit) {
                        case RepoOpInit.RepoInitItem.AddCodeBranch addCodeBranch ->
                                doAddCodeBranch(c);
                        case RepoOpInit.RepoInitItem.AddEmbeddings addEmbeddings ->
                                doAddEmbeddings(c);
                    }
                });

        return c;
    }

    private void doAddCodeBranch(RepoOpInit c) {
        var key = c.retrieveSessionKey();
        CommitDiff.AddCodeBranchArgs addCodeBranchArgs = CommitDiff.AddCodeBranchArgs.builder()
                .gitRepoPath(c.repoDataOrThrow().url())
                .branchName(c.repoDataOrThrow().branchName())
                .sessionKey(key)
                .build();

        var added = commitDiff.addCodeBranch(
                addCodeBranchArgs);

        assertions.assertSoftly(added.isOk(), "Could not add code branch.", "Added code branch successfully");
        added.e()
                .filter(cde -> Optional.ofNullable(cde.errors()).map(l -> !l.isEmpty()).orElse(false))
                .ifPresent(err -> assertions.assertSoftly(false, "Error on add code branch: %s"
                .formatted(added.e().get().getMessage()), "Add code branch completed successfully."));
    }

    private void doAddEmbeddings(RepoOpInit c) {
        var key = c.retrieveSessionKey();
        CommitDiff.AddEmbeddingsArgs addEmbeddings = CommitDiff.AddEmbeddingsArgs.builder()
                .gitRepoPath(c.repoDataOrThrow().url())
                .branchName(c.repoDataOrThrow().branchName())
                .sessionKey(key)
                .build();

        var added = commitDiff.addEmbeddings(
                addEmbeddings);


        assertions.assertSoftly(added.isOk(), "Could not add embeddings for code branch.",
                "Added embeddings for code branch successfully");
        added.e()
                .filter(cde -> Optional.ofNullable(cde.errors()).map(l -> !l.isEmpty()).orElse(false))
                .ifPresent(err -> assertions.assertSoftly(false, "Error on add embeddings: %s"
                .formatted(added.e().get().getMessage()), "Add embeddings completed successfully."));
    }

    @Override
    public Class<RepoOpInit> clzz() {
        return RepoOpInit.class;
    }

}
