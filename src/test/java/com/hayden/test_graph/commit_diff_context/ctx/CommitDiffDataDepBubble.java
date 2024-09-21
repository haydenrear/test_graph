package com.hayden.test_graph.commit_diff_context.ctx;

import com.hayden.test_graph.commit_diff_context.data_dep.CommitDiffDataDepBubbleNode;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.data_dep.ctx.DataDepMeta;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.stereotype.Component;

import java.util.List;

public record CommitDiffDataDepBubble() implements DataDepBubble {
    @Override
    public MetaCtx bubble() {
        return new DataDepMeta(this);
    }

    @Override
    public Class<? extends MetaCtx> bubbleClazz() {
        return DataDepMeta.class;
    }

    @Override
    public boolean executableFor(GraphNode n) {
        return n instanceof CommitDiffDataDepBubbleNode;
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
