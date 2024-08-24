package com.hayden.test_graph.init.exec.single;

import com.hayden.test_graph.graph.TestGraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;

public interface InitNode<I extends InitCtx> extends TestGraphNode<I, InitBubble> {
}