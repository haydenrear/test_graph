package com.hayden.test_graph.test_init;

import com.hayden.test_graph.init.exec.single.InitNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ResettableThread
public class TestInitCtxNode implements InitNode<TestInitCtx> {

    @Autowired
    MockRegister mockRegister;

    @Override
    public TestInitCtx exec(TestInitCtx c, MetaCtx h) {
        mockRegister.mocks.add(this.getClass());
        System.out.println();
        return InitNode.super.exec(c, h);
    }

    @Override
    public Class<? extends TestInitCtx> clzz() {
        return TestInitCtx.class;
    }

    @Override
    public List<Class<? extends InitNode<TestInitCtx>>> dependsOn() {
        return List.of(TestInitCtxDepNode.class);
    }
}
