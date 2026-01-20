package com.hayden.test_graph.commit_diff_context.hg;

import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssertNode;
import com.hayden.test_graph.commit_diff_context.assert_nodes.parent.CommitDiffAssertParentCtx;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpBubble;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.stereotype.Component;

//@Component
@ResettableThread
public class CommitDiffInitAssertTestGraphEdge
//        implements CommitDiffAssertNode
{

//    @Override
//    public Class<RepoOpBubble> bubbleClazz() {
//        return RepoOpBubble.class;
//    }
//
//    @Override
//    public Class<CommitDiffAssertParentCtx> initClazz() {
//        return CommitDiffAssertParentCtx.class;
//    }
//
//    /**
//     * Example hypergraph edge with parent ctx.
//     * @param setOn
//     * @param setFrom
//     */
//    @Override
//    public void set(CommitDiffAssertParentCtx setOn, RepoOpBubble setFrom) {
//        setFrom.getRepoInit().optional()
//                .ifPresent(repoInit -> setFrom.getRepoInit().swap(repoInit));
//    }
//
}
