package com.hayden.test_graph.data_dep.exec.bubble;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.graph.node.HyperGraphBubbleNode;
import com.hayden.test_graph.graph.node.HyperGraphTestNode;
import com.hayden.test_graph.init.exec.bubble.InitBubbleNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.List;

public interface DataDepBubbleNode<D extends DataDepBubble> extends HyperGraphTestNode<D, MetaCtx> {

    @Override
    default List<Class<? extends HyperGraphBubbleNode<? extends HyperGraphContext, MetaCtx>>> dependsOn() {
//        return List.of(InitBubbleNode.class); // TODO: how autoconfigured should this be ??? any then should? Then > Given > When and anything < Then idempotent on anything < Then ???
        return List.of();
    }

}
