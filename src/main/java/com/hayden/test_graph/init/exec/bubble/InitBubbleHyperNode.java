package com.hayden.test_graph.init.exec.bubble;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.graph.TestGraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.exec.InitExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ThreadScope
public class InitBubbleHyperNode implements InitBubbleNode {

    @Override
    public InitBubble mapCtx(InitBubble ctx) {
        return ctx;
    }

    @Override
    public InitBubble exec(InitBubble initBubble) {
        return initBubble;
    }


    @Override
    public Class<InitBubble> clzz() {
        return InitBubble.class;
    }

    @Override
    public List<Class<? extends TestGraphNode<InitBubble, MetaCtx>>> dependsOn() {
        return List.of();
    }

}
