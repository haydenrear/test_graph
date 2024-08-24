package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;

public interface HyperGraphTestNode<T extends HyperGraphContext<H>, H extends HyperGraphContext<H>>
        extends HyperGraphNode<T, H>, TestGraphNode<T, H> {

}
