package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.graph.MetaGraph;

import java.util.List;

public interface HyperGraph<C extends TestGraphContext<T>, T extends HyperGraphContext, H extends HyperGraphNode> extends Graph<C, T, H> {

    List<C> forBubbling();



}
