package com.hayden.test_graph.commit_diff_context.assert_nodes.parent;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssert;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
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

    private final ContextValue<RepoOpInit.RepositoryData> repoUrl;
    private final ContextValue<RepoOpInit.GraphQlQueries> graphQlQueries;
    private final ContextValue<Boolean> validated;

    private CommitDiffCtxParentBubble commitDiffAssertBubble;


    @Autowired
    @ResettableThread
    public void setRepoOpBubble(CommitDiffCtxParentBubble commitDiffAssertBubble) {
        this.commitDiffAssertBubble = commitDiffAssertBubble;
    }

    public CommitDiffAssertParentCtx() {
        this(ContextValue.empty(), ContextValue.empty(), ContextValue.empty());
    }

    @Override
    public CommitDiffCtxParentBubble bubble() {
        return commitDiffAssertBubble;
    }

    @Override
    public Class<? extends AssertBubble> bubbleClazz() {
        return CommitDiffCtxParentBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffCtxParentAssertNode;
    }

    public ContextValue<RepoOpInit.RepositoryData> repoUrl() {
        return repoUrl;
    }

    @Override
    public boolean isLeafNode() {
        return false;
    }
}
