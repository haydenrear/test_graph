package com.hayden.test_graph.commit_diff_context.assert_nodes.indexing;

import com.hayden.test_graph.assert_g.exec.single.AssertNode;
import com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.ctx.CommitDiffContextIndexingAssertCtx;

public interface CommitDiffContextIndexingAssertNode extends AssertNode<CommitDiffContextIndexingAssertCtx> {

    default Class<? extends CommitDiffContextIndexingAssertCtx> clzz() {
        return CommitDiffContextIndexingAssertCtx.class;
    }
}
