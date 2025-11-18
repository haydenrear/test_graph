package com.hayden.test_graph.commit_diff_context.init.indexing;

import com.hayden.test_graph.commit_diff_context.init.indexing.ctx.IndexingK3sInit;
import com.hayden.test_graph.init.exec.single.InitNode;

public interface IndexingK3sInitNode extends InitNode<IndexingK3sInit> {

    default Class<? extends IndexingK3sInit> clzz() {
        return IndexingK3sInit.class;
    }
}
