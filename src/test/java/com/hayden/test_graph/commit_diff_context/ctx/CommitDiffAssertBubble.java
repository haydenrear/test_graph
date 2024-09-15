package com.hayden.test_graph.commit_diff_context.ctx;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.assert_g.ctx.AssertMeta;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssertBubbleNode;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.List;

public record CommitDiffAssertBubble() implements AssertBubble {
    @Override
    public MetaCtx bubble() {
        return new AssertMeta(this);
    }

    @Override
    public Class<? extends MetaCtx> bubbleClazz() {
        return AssertMeta.class;
    }

    @Override
    public boolean executableFor(GraphNode n) {
        return n instanceof CommitDiffAssertBubbleNode;
    }

    @Override
    public boolean toSet(TestGraphContext context) {
        return false;
    }

    @Override
    public void doSet(TestGraphContext context) {
    }

    @Override
    public boolean isLeafNode() {
        return false;
    }

}
