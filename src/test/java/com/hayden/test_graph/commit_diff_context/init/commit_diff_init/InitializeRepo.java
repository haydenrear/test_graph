package com.hayden.test_graph.commit_diff_context.init.commit_diff_init;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InitializeRepo implements CommitDiffInitNode {

    @Autowired
    CommitDiff commitDiff;


    @Override
    @Idempotent
    public CommitDiffInit exec(CommitDiffInit c, MetaCtx h) {
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
    public Class<? extends CommitDiffInit> clzz() {
        return CommitDiffInit.class;
    }

    @Override
    public List<Class> dependsOn() {
        return List.of();
    }
}
