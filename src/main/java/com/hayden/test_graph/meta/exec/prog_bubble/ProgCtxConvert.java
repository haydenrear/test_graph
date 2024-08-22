package com.hayden.test_graph.meta.exec.prog_bubble;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;

public interface ProgCtxConvert<T extends HyperGraphContext> {

    MetaCtx visit(T t, MetaCtx ctx);

}
