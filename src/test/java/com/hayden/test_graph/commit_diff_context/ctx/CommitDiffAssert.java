package com.hayden.test_graph.commit_diff_context.ctx;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.assert_g.ctx.AssertCtx;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssertNode;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.stereotype.Component;


@Component
@ThreadScope
public record CommitDiffAssert() implements AssertCtx {

    @Override
    public CommitDiffAssertBubble bubble() {
        return new CommitDiffAssertBubble();
    }

    @Override
    public Class<? extends AssertBubble> bubbleClazz() {
        return CommitDiffAssertBubble.class;
    }

    @Override
    public boolean executableFor(GraphNode n) {
        return n instanceof CommitDiffAssertNode;
    }

    @Override
    public boolean toSet(TestGraphContext context) {
        return false;
    }

    @Override
    public void doSet(TestGraphContext context) {
    }

}
