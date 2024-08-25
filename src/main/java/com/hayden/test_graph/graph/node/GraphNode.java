package com.hayden.test_graph.graph.node;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.utilitymodule.sort.GraphSort;

public interface GraphNode<T extends TestGraphContext<H>, H extends HyperGraphContext>
        extends GraphSort.GraphSortable,
                GraphExec.GraphExecNode<T, H> {


}
