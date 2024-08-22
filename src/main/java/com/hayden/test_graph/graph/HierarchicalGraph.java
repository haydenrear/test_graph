package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.ContextValue;

public interface HierarchicalGraph {

    interface HasChildGraph<G extends Graph> extends HierarchicalGraph {
        ContextValue<G> child();
    }

    interface HasParentGraph<G extends Graph> extends HierarchicalGraph {
        ContextValue<G> parent();
    }

}
