package com.hayden.test_graph.graph.edge;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;

public interface PostReduceMetaGraphEdge<T extends HyperGraphContext<U>, U extends MetaCtx> extends MetaGraphEdge<T, U> {
}