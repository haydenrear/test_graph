package com.hayden.test_graph.meta.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.graph.node.HyperGraphNode;
import com.hayden.test_graph.meta.exec.prog_bubble.MetaProgNode;
import com.hayden.test_graph.thread.ThreadScope;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Stack;

@Component
@ThreadScope
public class MetaProgCtx implements MetaCtx {

    @Delegate
    Stack<MetaCtx> delegates = new Stack<>();

    @Override
    public MetaCtx bubble() {
        return this;
    }

    @Override
    public boolean executableFor(GraphNode n) {
        return n instanceof HyperGraphNode<?, ?>
                && !(n instanceof MetaProgNode<?>);
    }

    @Override
    public List<Class<? extends TestGraphContext<MetaCtx>>> dependsOn() {
        return List.of();
    }

    @Override
    public boolean toSet(TestGraphContext context) {
        return false;
    }

    @Override
    public void doSet(TestGraphContext context) {

    }

    @Override
    public boolean isExecutable() {
        return false;
    }

    @Override
    public ContextValue<TestGraphContext> child() {
        return ContextValue.empty();
    }

    @Override
    public ContextValue<TestGraphContext> parent() {
        return ContextValue.empty();
    }

    @Override
    public Stack<? extends HyperGraphContext> prev() {
        return delegates;
    }
}
