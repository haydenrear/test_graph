package com.hayden.test_graph.meta.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.node.HyperGraphBubbleNode;
import com.hayden.test_graph.meta.exec.prog_bubble.MetaProgNode;

import java.util.Stack;

public interface MetaCtx extends HyperGraphContext<MetaCtx> {

    boolean executableFor(MetaProgNode n);

    Stack<? extends HyperGraphContext> prev();

    default boolean toSet(TestGraphContext context) {
        return false;
    }

    default void doSet(TestGraphContext context) {}

    default boolean isLeafNode() {
        return false;
    }


    default ContextValue<TestGraphContext> child() {
        return ContextValue.empty();
    }

    @Override
    default ContextValue<TestGraphContext> parent() {
        return ContextValue.empty();
    }
}
