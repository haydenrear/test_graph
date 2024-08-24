package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.utilitymodule.sort.GraphSort;

import java.util.List;

public interface GraphNode<T extends TestGraphContext<H>, H extends HyperGraphContext>
        extends GraphSort.GraphSortable,
                GraphExec.GraphExecNode<T, H> {

}
