package com.hayden.test_graph.init.exec.bubble;

import com.hayden.test_graph.graph.node.TestGraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ThreadScope
public class DefaultInitBubbleNode implements InitBubbleNode {

    @Override
    public Class<InitBubble> clzz() {
        return InitBubble.class;
    }

    @Override
    public List<Class<? extends TestGraphNode<InitBubble, MetaCtx>>> dependsOn() {
        return List.of();
    }

}
