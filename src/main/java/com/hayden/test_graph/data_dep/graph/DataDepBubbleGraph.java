package com.hayden.test_graph.data_dep.graph;

import com.hayden.test_graph.graph.HyperTestGraph;
import com.hayden.test_graph.graph.node.HyperGraphBubbleNode;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ResettableThread
public class DataDepBubbleGraph implements HyperTestGraph<DataDepBubble> {

    @Override
    public List<? extends HyperGraphBubbleNode<DataDepBubble>> sortedNodes() {
        return List.of();
    }


}
