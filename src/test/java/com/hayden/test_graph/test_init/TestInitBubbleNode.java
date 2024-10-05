package com.hayden.test_graph.test_init;

import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.exec.bubble.InitBubbleNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ResettableThread
public class TestInitBubbleNode implements InitBubbleNode {

    @Autowired
    MockRegister mockRegister;

    @Override
    public InitBubble exec(InitBubble c, MetaCtx h) {
        mockRegister.mocks.add(this.getClass());
        return InitBubbleNode.super.exec(c, h);
    }

    @Override
    public Class<? extends InitBubble> clzz() {
        return TestInitBubble.class;
    }

    @Override
    public List<Class> dependsOn() {
        return List.of();
    }
}
