package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;

import java.util.List;

public interface TestGraph<T extends TestGraphContext<H>, H extends HyperGraphContext, TN extends TestGraphNode<T>> extends Graph<T, H, TN> {

    /**
     * Bubble the hypergraph context.
     * @return bubbled hypergraph context - this is the context that is shared between contexts in the MetaContext.
     */
    H bubble();

    T ctx();

    void initialize(HyperGraph hg, H hyperGraphContext,
                    HyperGraphNode hgn);

}
