package com.hayden.test_graph.init.ctx;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import lombok.experimental.Delegate;

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
