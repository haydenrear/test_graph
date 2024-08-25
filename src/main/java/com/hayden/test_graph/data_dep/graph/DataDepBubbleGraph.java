package com.hayden.test_graph.data_dep.graph;

import com.hayden.test_graph.graph.HyperTestGraph;
import com.hayden.test_graph.graph.node.HyperGraphNode;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ThreadScope
public class DataDepBubbleGraph implements HyperTestGraph<DataDepBubble, MetaCtx> {
    @Override
    public List<? extends HyperGraphNode<DataDepBubble, MetaCtx>> sortedNodes() {
        return List.of();
    }


}
