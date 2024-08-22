package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.utilitymodule.sort.GraphSort;

import java.util.List;

public interface TestGraphNode<T extends TestGraphContext> extends GraphSort.GraphSortable, GraphExec.GraphExecNode<T> {

    List<Class<? extends TestGraphNode<T>>> dependsOn();

}
