package com.hayden.test_graph.data_dep.exec.bubble;

import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.graph.HyperGraphNode;
import com.hayden.test_graph.graph.TestGraphNode;
import com.hayden.test_graph.init.exec.bubble.InitBubbleNode;

import java.util.List;

public interface DataDepBubbleNode<D extends DataDepBubble> extends HyperGraphNode<D> {

    @Override
    default List<Class<? extends HyperGraphNode>> dependsOnHyperNodes() {
        return List.of(InitBubbleNode.class);
    }

}
