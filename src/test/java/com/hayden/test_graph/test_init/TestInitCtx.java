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

import java.util.Optional;

@Component
@ResettableThread
public class TestInitCtx implements InitCtx {


    @Autowired
    MockRegister mockRegister;

    ContextValue<TestGraphContext> parent = ContextValue.empty();
    ContextValue<TestGraphContext> child = ContextValue.empty();

    @Override
    public TestInitBubble bubble() {
        mockRegister.mocks.add(this.getClass());
        return new TestInitBubble();
    }

    @Override
    public Class<? extends InitBubble> bubbleClazz() {
        return TestInitBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof InitNode<?>;
    }

    @Override
    public boolean toSet(TestGraphContext context) {
        return context instanceof TestInitChildCtx || context instanceof TestInitParentCtx;
    }

    @Override
    public void doSet(TestGraphContext context) {
        if (context instanceof TestInitChildCtx c) {
            child.swap(c);
        } else if (context instanceof TestInitParentCtx c) {
            parent.swap(c);
        }
    }

    @Override
    public boolean isLeafNode() {
        return false;
    }

    @Override
    public ContextValue<TestGraphContext> parent() {
        return parent;
    }

    @Override
    public Optional<Class<? extends TestGraphContext>> parentTy() {
        return Optional.of(TestInitParentCtx.class) ;
    }
}
