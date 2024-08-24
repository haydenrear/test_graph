package com.hayden.test_graph.test_init;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.ctx.InitMeta;
import com.hayden.test_graph.init.exec.single.InitNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ThreadScope
public class TestInitChildCtx implements InitCtx {

    ContextValue<TestGraphContext> parent = ContextValue.empty();

    @Override
    public TestInitBubble bubble() {
        return new TestInitBubble();
    }

    @Override
    public boolean executableFor(GraphNode n) {
        return n instanceof InitNode<?>;
    }

    @Override
    public boolean toSet(TestGraphContext context) {
        return context instanceof TestInitCtx;
    }

    @Override
    public void doSet(TestGraphContext context) {
        parent.set(context);
    }

    @Override
    public boolean isLeafNode() {
        return true;
    }

    @Override
    public ContextValue<TestGraphContext> child() {
        return ContextValue.empty();
    }

    @Override
    public ContextValue<TestGraphContext> parent() {
        return parent;
    }

    @Override
    public List<Class<? extends TestGraphContext<InitBubble>>> dependsOn() {
        return List.of();
    }

    @Override
    public Optional<Class<? extends TestGraphContext>> parentTy() {
        return Optional.of(TestInitCtx.class);
    }
}
