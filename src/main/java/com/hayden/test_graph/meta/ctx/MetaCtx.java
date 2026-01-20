package com.hayden.test_graph.meta.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.node.HyperGraphBubbleNode;
import com.hayden.test_graph.meta.exec.prog_bubble.MetaProgNode;
import jakarta.validation.constraints.Null;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public interface MetaCtx extends HyperGraphContext<MetaCtx> {

    @Nullable HyperGraphContext<MetaCtx> getBubbled();

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

    default boolean didRun(Class<? extends TestGraphContext> check) {
        return false;
    }

    void ran(TestGraphContext check);

}
