package com.hayden.test_graph.graph.edge;

import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.function.Predicate;

public interface GraphEdge<T, U> {

    /**
     * Provides
     * @param transform
     * @param ctx
     * @return
     */
    T edge(T transform, U ctx);

    Predicate<? super Object> from();

    Predicate<? super Object> to();

}
