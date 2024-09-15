package com.hayden.test_graph.assert_g.exec.single;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.assert_g.ctx.AssertCtx;
import com.hayden.test_graph.graph.node.TestGraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;

public interface AssertNode<I extends AssertCtx> extends TestGraphNode<I, AssertBubble> {
}