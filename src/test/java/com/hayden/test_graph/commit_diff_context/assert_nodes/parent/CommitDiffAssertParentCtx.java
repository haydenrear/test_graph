package com.hayden.test_graph.commit_diff_context.assert_nodes.parent;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssert;
import com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op.RepoOpAssertBubble;
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

    private RepoOpAssertBubble commitDiffAssertBubble;


    @Autowired
    public void setRepoOpBubble(RepoOpAssertBubble commitDiffAssertBubble) {
        this.commitDiffAssertBubble = commitDiffAssertBubble;
    }

    public CommitDiffAssertParentCtx() {
        this(ContextValue.empty(), ContextValue.empty());
    }

    @Override
    public RepoOpAssertBubble bubble() {
        return commitDiffAssertBubble;
    }

    @Override
    public Class<? extends AssertBubble> bubbleClazz() {
        return RepoOpAssertBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffAssertParentCtx;
    }

    public ContextValue<RepoOpInit.RepositoryData> repoUrl() {
        return repoUrl;
    }


}
