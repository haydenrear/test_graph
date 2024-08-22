package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;

import java.util.List;

public interface HyperGraphNode<T extends HyperGraphContext<H>, H extends HyperGraphContext> extends GraphNode<T, H> {

    default List<Class<? extends HyperGraphNode>> dependsOnHyperNodes() {
        return List.of();
    }

    T mapCtx(T ctx);

}
