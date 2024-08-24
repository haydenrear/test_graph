package com.hayden.test_graph.test_init;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitMeta;
import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.List;

public class TestInitBubble implements InitBubble {

    @Override
    public MetaCtx bubble() {
        return new InitMeta(this);
    }

    @Override
    public boolean executableFor(GraphNode n) {
        return true;
    }

    @Override
    public boolean toSet(TestGraphContext context) {
        return false;
    }

    @Override
    public void doSet(TestGraphContext context) {

    }

    @Override
    public boolean isLeafNode() {
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
    public List<Class<? extends TestGraphContext<MetaCtx>>> dependsOn() {
        return List.of();
    }
}
