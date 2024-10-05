package com.hayden.test_graph.graph.edge;

import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.function.Predicate;

public interface GraphEdge<T, U> {

    /**
     * Provides
     * @param first
     * @param second
     * @return
     */
    U edge(T first, U second);

    Predicate from();

    Predicate to();

}
