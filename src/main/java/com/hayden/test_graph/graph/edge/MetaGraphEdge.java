package com.hayden.test_graph.graph.edge;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;

public interface MetaGraphEdge<T extends HyperGraphContext, U extends MetaCtx> extends GraphEdge<U, T> {
}
