package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;

public interface Graph<H extends HyperGraphContext> {


    TestGraphSort sortingAlgorithm();

    GraphAutoDetect allNodes();


}
