package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;

import java.util.List;
import java.util.Map;

public interface TestGraph<T extends TestGraphContext<H>, H extends HyperGraphContext> extends Graph<H> {

    List<? extends T> sortedCtx(Class<? extends T> clzz);

    Map<Class<? extends T>, List<? extends GraphNode<T, H>>> sortedNodes();

    List<SubGraph<InitCtx, InitBubble>> subGraphs();

}
