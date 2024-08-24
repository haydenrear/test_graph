package com.hayden.test_graph.ctx;

import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.utilitymodule.sort.GraphSort;

public non-sealed interface TestGraphContext<H extends HyperGraphContext>
        extends GraphContext<TestGraphContext<H>>,
                HierarchicalContext,
                GraphSort.GraphSortable<TestGraphContext<H>> {

    H bubble();

    boolean executableFor(GraphNode n);

}