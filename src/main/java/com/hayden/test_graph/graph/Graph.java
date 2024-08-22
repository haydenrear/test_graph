package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;

import java.util.List;

public interface Graph<T extends TestGraphContext<H>, H extends HyperGraphContext, NT extends TestGraphNode> {

    Graph<T, H, NT> fromSorted(List<NT> nodes);

    List<? extends TestGraphNode<T>> sortedNodes();

    GraphAutoDetect allNodes();

}
