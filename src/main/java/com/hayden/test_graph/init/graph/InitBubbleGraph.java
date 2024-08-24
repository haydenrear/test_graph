package com.hayden.test_graph.init.graph;

import com.hayden.test_graph.graph.*;
import com.hayden.test_graph.graph.service.GraphAutoDetect;
import com.hayden.test_graph.graph.service.LazyGraphAutoDetect;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.exec.bubble.InitBubbleNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ThreadScope
public class InitBubbleGraph implements HyperTestGraph<InitBubble, MetaCtx> {

    @Autowired @Lazy
    LazyGraphAutoDetect nodesProvider;

    @Autowired
    TestGraphSort graphSort;

    @Autowired
    @ThreadScope
    List<InitBubbleNode> sortedNodes;

    @Override
    public List<? extends InitBubbleNode> sortedNodes() {
        return sortedNodes;
    }

    @Override
    public TestGraphSort sortingAlgorithm() {
        return graphSort;
    }


    @Override
    public GraphAutoDetect allNodes() {
        return nodesProvider.getAutoDetect();
    }

}
