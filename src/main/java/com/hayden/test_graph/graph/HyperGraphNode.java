package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;

import java.util.List;

public interface HyperGraphNode<T extends HyperGraphContext> extends TestGraphNode<T> {

    default List<Class<? extends HyperGraphNode>> dependsOnHyperNodes() {
        return List.of();
    }

    T mapCtx(T ctx);

    void collectCtx();

}
