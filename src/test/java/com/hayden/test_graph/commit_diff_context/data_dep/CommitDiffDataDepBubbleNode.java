package com.hayden.test_graph.commit_diff_context.data_dep;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.data_dep.exec.bubble.DataDepBubbleNode;
import com.hayden.test_graph.graph.node.HyperGraphBubbleNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.List;

public interface CommitDiffDataDepBubbleNode extends DataDepBubbleNode<CommitDiffDataDepBubble> {
    @Override
    default List<Class<? extends HyperGraphBubbleNode<? extends HyperGraphContext>>> dependsOn() {
        return DataDepBubbleNode.super.dependsOn();
    }
}
