package com.hayden.test_graph.commit_diff_context.ctx;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.assert_g.ctx.AssertCtx;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssertNode;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@ResettableThread
@RequiredArgsConstructor
public class CommitDiffAssert implements AssertCtx {
    private final ContextValue<CommitDiffInit.RepositoryData> repoUrl;

    private CommitDiffAssertBubble commitDiffAssertBubble;

    @Autowired
    public void setCommitDiffAssertBubble(CommitDiffAssertBubble commitDiffAssertBubble) {
        this.commitDiffAssertBubble = commitDiffAssertBubble;
    }

    public CommitDiffAssert() {
        this(ContextValue.empty());
    }

    @Override
    public CommitDiffAssertBubble bubble() {
        return commitDiffAssertBubble;
    }

    @Override
    public Class<? extends AssertBubble> bubbleClazz() {
        return CommitDiffAssertBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffAssertNode;
    }

    public ContextValue<CommitDiffInit.RepositoryData> repoUrl() {
        return repoUrl;
    }


}
