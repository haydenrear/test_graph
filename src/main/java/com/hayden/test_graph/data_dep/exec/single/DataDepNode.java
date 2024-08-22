package com.hayden.test_graph.data_dep.exec.single;

import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.data_dep.ctx.DataDepCtx;
import com.hayden.test_graph.graph.TestGraphNode;

public interface DataDepNode<D extends DataDepCtx> extends TestGraphNode<D, DataDepBubble> {
}
