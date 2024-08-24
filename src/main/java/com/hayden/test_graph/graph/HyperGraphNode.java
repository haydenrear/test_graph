package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.List;

public interface HyperGraphNode<T extends HyperGraphContext<H>, H extends HyperGraphContext> extends GraphNode<T, H> {

    default List<Class<? extends HyperGraphNode>> dependsOnHyperNodes() {
        return List.of();
    }

    default T preMap(T ctx, MetaCtx metaCtx) {
        return ctx;
    }

    default T postMap(T ctx, MetaCtx metaCtx) {
        return ctx;
    }

}
