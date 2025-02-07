package com.hayden.test_graph.commit_diff_context.hg;

import com.hayden.test_graph.commit_diff_context.assert_nodes.parent.CommitDiffAssertParentCtx;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpBubble;
import com.hayden.test_graph.graph.edge.FromBubbleEdgeAssert;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.stereotype.Component;

@Component
@ResettableThread
public class CommitDiffInitAssertTestGraphEdge implements FromBubbleEdgeAssert<CommitDiffAssertParentCtx, RepoOpBubble> {

    @Override
    public Class<RepoOpBubble> bubbleClazz() {
        return RepoOpBubble.class;
    }

    @Override
    public Class<CommitDiffAssertParentCtx> initClazz() {
        return CommitDiffAssertParentCtx.class;
    }

    @Override
    public void set(CommitDiffAssertParentCtx setOn, RepoOpBubble setFrom) {
        setFrom.repositoryData().optional()
                .ifPresent(setOn.repoUrl()::swap);
    }

}
