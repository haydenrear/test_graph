package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;

/**
 * A hypergraph is a graph made up of other graphs. In this case it facilitates the bubbling of the various sorts of computations.
 * @param <T>
 */
public interface HyperGraph<T extends HyperGraphContext> extends Graph {


}
