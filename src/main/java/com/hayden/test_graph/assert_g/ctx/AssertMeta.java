package com.hayden.test_graph.assert_g.ctx;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import lombok.experimental.Delegate;

import java.util.Stack;

public class AssertMeta implements MetaCtx {

    @Delegate
    AssertBubble bubble;

    final Stack<HyperGraphContext<MetaCtx>> prev = new Stack<>();

    public AssertMeta(AssertBubble bubble) {
        this.bubble = bubble;
        prev.push(bubble);
    }

    public Stack<? extends HyperGraphContext> prev() {
        return prev;
    }

}
