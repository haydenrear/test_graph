package com.hayden.test_graph.meta.ctx;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;

import java.util.Stack;

public interface MetaCtx extends HyperGraphContext<MetaCtx>, TestGraphContext<MetaCtx> {

    Stack<? extends HyperGraphContext> prev();

}
