package com.hayden.test_graph.commit_diff_context.ctx;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.assert_g.ctx.AssertMeta;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssertBubbleNode;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.stereotype.Component;

@Component
@ThreadScope
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
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffAssertBubbleNode;
    }

}
