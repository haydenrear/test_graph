package com.hayden.test_graph.edge;

public interface GraphEdge<T, U> {

    /**
     * Provides
     * @param first
     * @param second
     * @return
     */
    U edge(T first, U second);

}
