package com.hayden.test_graph.assert_g.graph;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.assert_g.exec.bubble.AssertBubbleNode;
import com.hayden.test_graph.graph.HyperTestGraph;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.exec.bubble.InitBubbleNode;
import com.hayden.test_graph.meta.LazyMetaGraphDelegate;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@ThreadScope
public class AssertBubbleGraph implements HyperTestGraph<AssertBubble, MetaCtx> {

    @Autowired @Lazy
    LazyMetaGraphDelegate nodesProvider;

    @Autowired
    TestGraphSort graphSort;

    @Autowired(required = false)
    @ThreadScope
    List<AssertBubbleNode> sortedNodes;

    @Override
    public List<? extends AssertBubbleNode> sortedNodes() {
        return Optional.ofNullable(sortedNodes).orElse(new ArrayList<>());
    }


}
