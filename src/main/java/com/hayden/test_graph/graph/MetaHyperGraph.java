package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.exec.single.MetaNode;

import java.util.List;

/**
 * Graph representing the bubbles from the TestGraphs.
 * @param <C>
 * @param <T>
 */
public interface MetaHyperGraph<C extends HyperGraphContext<T>, T extends HyperGraphContext<MetaCtx>> extends Graph {

    List<MetaCtx> bubble();

    List<? extends MetaNode> sortedNodes();
}
