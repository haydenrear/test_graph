package com.hayden.test_graph.graph;

import com.hayden.test_graph.graph.service.GraphAutoDetect;
import com.hayden.test_graph.graph.service.TestGraphSort;

public interface Graph {


    TestGraphSort sortingAlgorithm();

    GraphAutoDetect allNodes();


}
