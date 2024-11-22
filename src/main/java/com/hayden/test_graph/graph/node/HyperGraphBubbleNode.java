package com.hayden.test_graph.graph.node;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.utilitymodule.sort.GraphSort;

import java.util.List;

public interface HyperGraphBubbleNode<T extends HyperGraphContext>
        extends GraphNode, GraphExec.GraphExecNode<T> {

    /**
     * Graph nodes in a large part classified by the contexts of the nodes.
     * @return
     */
    Class<? extends T> clzz();

    default T preMap(T ctx, MetaCtx metaCtx) {
        return ctx;
    }

    default T postMap(T ctx, MetaCtx metaCtx) {
        return ctx;
    }

}
