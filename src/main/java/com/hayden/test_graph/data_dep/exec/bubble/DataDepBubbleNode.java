package com.hayden.test_graph.data_dep.exec.bubble;

import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.graph.node.HyperGraphNode;
import com.hayden.test_graph.graph.node.HyperGraphTestNode;
import com.hayden.test_graph.init.exec.bubble.InitBubbleNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.List;

public interface DataDepBubbleNode<D extends DataDepBubble> extends HyperGraphTestNode<D, MetaCtx> {

    @Override
    default List<Class<? extends HyperGraphNode>> dependsOnHyperNodes() {
        return List.of(InitBubbleNode.class);
    }

}
