package com.hayden.test_graph.commit_diff_context.assert_nodes.parent;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssert;
import com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op.RepoOpBubble;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@ResettableThread
@RequiredArgsConstructor
@Getter
public class CommitDiffAssertParentCtx implements CommitDiffAssert {

    private final ContextValue<CommitDiffInit.RepositoryData> repoUrl;
    private final ContextValue<CommitDiffInit.GraphQlQueries> graphQlQueries;

    private RepoOpBubble commitDiffAssertBubble;


    @Autowired
    public void setRepoOpBubble(RepoOpBubble commitDiffAssertBubble) {
        this.commitDiffAssertBubble = commitDiffAssertBubble;
    }

    public CommitDiffAssertParentCtx() {
        this(ContextValue.empty(), ContextValue.empty());
    }

    @Override
    public RepoOpBubble bubble() {
        return commitDiffAssertBubble;
    }

    @Override
    public Class<? extends AssertBubble> bubbleClazz() {
        return RepoOpBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffAssertParentCtx;
    }

    public ContextValue<CommitDiffInit.RepositoryData> repoUrl() {
        return repoUrl;
    }


}
