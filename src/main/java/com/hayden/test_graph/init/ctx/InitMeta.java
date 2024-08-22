package com.hayden.test_graph.init.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.GraphNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

import java.util.List;
import java.util.Stack;

public class InitMeta implements MetaCtx {

    @Delegate
    InitBubble bubble;

    final Stack<HyperGraphContext<MetaCtx>> prev = new Stack<>();

    public InitMeta(InitBubble bubble) {
        this.bubble = bubble;
        prev.push(bubble);
    }

    public Stack<? extends HyperGraphContext> prev() {
        return prev;
    }

}
