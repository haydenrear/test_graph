package com.hayden.test_graph.init.graph;

import com.hayden.test_graph.graph.*;
import com.hayden.test_graph.meta.LazyMetaGraphDelegate;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.exec.bubble.InitBubbleNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@ResettableThread
public class InitBubbleGraph implements HyperTestGraph<InitBubble, MetaCtx> {

    @Autowired @Lazy
    LazyMetaGraphDelegate nodesProvider;

    @Autowired
    TestGraphSort graphSort;

    @Autowired(required = false)
    @ResettableThread
    List<InitBubbleNode> sortedNodes;

    @Override
    public List<? extends InitBubbleNode> sortedNodes() {
        return Optional.ofNullable(sortedNodes).orElse(new ArrayList<>());
    }


}
