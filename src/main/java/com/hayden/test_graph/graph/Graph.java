package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.graph.service.GraphAutoDetect;
import com.hayden.test_graph.graph.service.TestGraphSort;

public interface Graph<H extends HyperGraphContext> {


    TestGraphSort sortingAlgorithm();

    GraphAutoDetect allNodes();


}
