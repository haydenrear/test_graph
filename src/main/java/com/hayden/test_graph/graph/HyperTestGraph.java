package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.graph.node.HyperGraphBubbleNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.List;

/**
 * A hypergraph is a graph made up of other graphs. In this case it facilitates the bubbling of the various sorts of computations.
 * @param <C>
 * @param <T>
 */
public interface HyperTestGraph<C extends HyperGraphContext> extends Graph {

    List<? extends HyperGraphBubbleNode<C>> sortedNodes();


}
