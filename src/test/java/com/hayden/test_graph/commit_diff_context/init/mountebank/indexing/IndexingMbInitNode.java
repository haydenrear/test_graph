package com.hayden.test_graph.commit_diff_context.init.mountebank.indexing;

import com.hayden.test_graph.commit_diff_context.init.mountebank.indexing.ctx.IndexingMbInitCtx;
import com.hayden.test_graph.init.mountebank.exec.MbInitNode;

public interface IndexingMbInitNode extends MbInitNode<IndexingMbInitCtx> {

    default Class<? extends IndexingMbInitCtx> clzz() {
        return IndexingMbInitCtx.class;
    }
}
