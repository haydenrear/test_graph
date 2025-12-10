package com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.CommitDiffContextIndexingAssertBubbleNode;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ResettableThread
@RequiredArgsConstructor
@Profile("indexing")
public class CommitDiffContextIndexingAssertBubble implements AssertBubble {

    @Getter
    private final ContextValue<CommitDiffContextIndexingAssertCtx> indexingAssertCtx;

    public CommitDiffContextIndexingAssertBubble() {
        this(ContextValue.empty());
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffContextIndexingAssertBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(CommitDiffContextIndexingAssertCtx.class);
    }

}
