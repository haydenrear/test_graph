package com.hayden.test_graph.init.exec.single;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.graph.TestGraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.stereotype.Component;

import java.util.List;

public interface InitNode<I extends InitCtx> extends TestGraphNode<I, InitBubble> {


}
