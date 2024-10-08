package com.hayden.test_graph.graph.node;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.List;

public interface HyperGraphNode<T extends HyperGraphContext<H>, H extends HyperGraphContext<MetaCtx>> extends GraphNode {

    default List<Class<? extends HyperGraphNode<? extends HyperGraphContext<H>, H>>> dependsOnHyperNodes() {
        return List.of();
    }

}
