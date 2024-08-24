package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;

/**
 * A hypergraph is a graph made up of other graphs. In this case it facilitates the bubbling of the various sorts of computations.
 * @param <C>
 * @param <T>
 */
public interface HyperGraph<C extends TestGraphContext<T>, T extends HyperGraphContext> extends Graph {


}
