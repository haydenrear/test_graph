package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;

import java.util.List;

/**
 * A hypergraph is a graph made up of other graphs. In this case it facilitates the bubbling of the various sorts of computations.
 * @param <C>
 * @param <T>
 */
public interface HyperTestGraph<C extends HyperGraphContext<T>, T extends HyperGraphContext<T>> extends Graph<T> {

    List<? extends HyperGraphNode<C, T>> sortedNodes();


}
