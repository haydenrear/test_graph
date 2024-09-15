package com.hayden.test_graph.commit_diff_context.init;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.ctx.CommitDiffInit;
import com.hayden.test_graph.commit_diff_context.service.CommitDiffContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.client.GraphQlTransportException;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.util.List;

@Component
public class InitializeRepo implements CommitDiffInitNode{

    @Autowired
    CommitDiffContext commitDiffContext;


    @Override
    public CommitDiffInit exec(CommitDiffInit c, MetaCtx h) {
        // clone repo, add to context
        return c.repoData().res()
                .map(rd -> {
                        commitDiffContext.addCodeBranch(
                                CommitDiffContext.AddCodeBranchArgs.builder()
                                        .gitRepoPath(c.repoDataOrThrow().url())
                                        .branchName(c.repoDataOrThrow().branchName())
                                        .commitMessage(c.userCodeDataOrThrow().commitMessage())
                                        .build()
                        );
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
