package com.hayden.test_graph.init.graph;

import com.hayden.test_graph.graph.*;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.exec.bubble.InitBubbleNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ThreadScope
public class InitBubbleGraph implements HyperGraph<InitBubble, MetaCtx, InitBubbleNode<InitBubble>> {

    @Autowired
    GraphAutoDetect nodesProvider;

    @ThreadScope
    @Autowired
    InitBubble bubble;

    List<InitCtx> ctx;

    @PostConstruct
    public void initialize() {
        this.ctx = nodesProvider.retrieveCtx(t -> t instanceof InitCtx c ? c : null);
    }

    @Override
    public InitBubbleGraph fromSorted(List<InitBubbleNode<InitBubble>> nodes) {
        return null;
    }

    @Override
    public List<? extends TestGraphNode<InitBubble>> sortedNodes() {
        return List.of();
    }

    @Override
    public GraphAutoDetect allNodes() {
        return nodesProvider;
    }

    @Override
    public List<InitBubble> forBubbling() {
        return List.of();
    }
}
