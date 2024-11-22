package com.hayden.test_graph.graph.edge;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;


public interface TestGraphEdge<T extends TestGraphContext<U>, U extends HyperGraphContext> extends GraphEdge<T, MetaCtx> {
}
