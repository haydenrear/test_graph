package com.hayden.test_graph.commit_diff_context.data_dep.indexing;

import com.hayden.test_graph.commit_diff_context.data_dep.indexing.ctx.CommitDiffContextIndexingDataDepCtx;
import com.hayden.test_graph.data_dep.exec.single.DataDepNode;

public interface CommitDiffContextIndexingDataDepNode extends DataDepNode<CommitDiffContextIndexingDataDepCtx> {

    default Class<? extends CommitDiffContextIndexingDataDepCtx> clzz() {
        return CommitDiffContextIndexingDataDepCtx.class;
    }
}
