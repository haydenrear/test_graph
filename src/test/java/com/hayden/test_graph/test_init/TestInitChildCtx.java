package com.hayden.test_graph.test_init;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.exec.single.InitNode;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ResettableThread
public class TestInitChildCtx implements InitCtx {

    @Autowired
    MockRegister mockRegister;

    ContextValue<TestGraphContext> parent = ContextValue.empty();

    @Override
    public TestInitBubble bubble() {
        mockRegister.mocks.add(this.getClass());
        return new TestInitBubble();
    }

    @Override
    public Class<TestInitBubble> bubbleClazz() {
        return TestInitBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof InitNode<?>;
    }

    @Override
    public boolean toSet(TestGraphContext context) {
        return context instanceof TestInitCtx;
    }

    @Override
    public void doSet(TestGraphContext context) {
        parent.swap(context);
    }

    @Override
    public boolean isLeafNode() {
        return true;
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
