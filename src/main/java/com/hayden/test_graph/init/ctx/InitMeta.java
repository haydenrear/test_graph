package com.hayden.test_graph.init.ctx;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.exec.prog_bubble.MetaProgNode;
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

    @Override
    public boolean executableFor(MetaProgNode n) {
        return true;
    }

    public Stack<? extends HyperGraphContext> prev() {
        return prev;
    }

}
