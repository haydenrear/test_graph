package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;

public interface TestGraphNode<T extends TestGraphContext<H>, H extends HyperGraphContext> extends GraphNode<T, H> {

    /**
     * Graph nodes in a large part classified by the contexts of the nodes.
     * @return
     */
    Class<? extends T> clzz();

}
