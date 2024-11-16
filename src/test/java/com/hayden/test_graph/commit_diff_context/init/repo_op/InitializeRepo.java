package com.hayden.test_graph.commit_diff_context.init.repo_op;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.CommitDiffInitNode;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InitializeRepo implements RepoOpInitNode {

    @Autowired
    CommitDiff commitDiff;


    @Override
    @Idempotent
    public RepoOpInit exec(RepoOpInit c, MetaCtx h) {
        // clone repo, add to context
        return c.repoData().res()
                .filterResult(rd -> rd.branchName() != null && rd.url() != null)
                .map(rd -> {
                        commitDiff.addCodeBranch(
                                CommitDiff.AddCodeBranchArgs.builder()
                                        .gitRepoPath(c.repoDataOrThrow().url())
                                        .branchName(c.repoDataOrThrow().branchName())
                                        .build());
                    return c;
                })
                .orElseRes(c);
    }

    @Override
    public Class<RepoOpInit> clzz() {
        return RepoOpInit.class;
    }

    @Override
    public List<Class> dependsOn() {
        return List.of();
    }
}
