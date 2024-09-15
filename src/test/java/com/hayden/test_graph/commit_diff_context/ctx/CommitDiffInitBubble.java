package com.hayden.test_graph.commit_diff_context.ctx;

import com.hayden.test_graph.commit_diff_context.init.CommitDiffInitBubbleNode;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitMeta;
import com.hayden.test_graph.meta.ctx.MetaCtx;

import java.util.List;

public record CommitDiffInitBubble() implements InitBubble {
    @Override
    public MetaCtx bubble() {
        return new InitMeta(this);
    }

    @Override
    public Class<? extends MetaCtx> bubbleClazz() {
        return InitMeta.class;
    }

    @Override
    public boolean executableFor(GraphNode n) {
        return n instanceof CommitDiffInitBubbleNode;
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
