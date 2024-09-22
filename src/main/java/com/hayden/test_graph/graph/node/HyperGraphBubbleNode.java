package com.hayden.test_graph.graph.node;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.List;

public interface HyperGraphBubbleNode<T extends HyperGraphContext<H>, H extends HyperGraphContext<MetaCtx>> extends GraphNode, GraphExec.GraphExecNode<T, H> {

    /**
     * Graph nodes in a large part classified by the contexts of the nodes.
     * @return
     */
    Class<? extends T> clzz();

    default List<Class<? extends HyperGraphBubbleNode<? extends HyperGraphContext<H>, H>>> dependsOnHyperNodes() {
        return List.of();
    }

    default T preMap(T ctx, MetaCtx metaCtx) {
        return ctx;
    }

    default T postMap(T ctx, MetaCtx metaCtx) {
        return ctx;
    }

}
