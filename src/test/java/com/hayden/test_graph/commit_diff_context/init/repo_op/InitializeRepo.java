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
import java.util.UUID;

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
                    var key = UUID.randomUUID().toString();
                    c.getCommitDiffData().set(new RepoOpInit.CommitDiffData(key));
                    commitDiff.addCodeBranch(
                            CommitDiff.AddCodeBranchArgs.builder()
                                    .gitRepoPath(c.repoDataOrThrow().url())
                                    .branchName(c.repoDataOrThrow().branchName())
                                    .sessionKey(key)
                                    .build());

                    return c;
                })
                .one()
                .orElseRes(c);
    }

    @Override
    public Class<RepoOpInit> clzz() {
        return RepoOpInit.class;
    }

}
