package com.hayden.test_graph.commit_diff_context.assert_nodes.parent;

import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssertBubble;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@ResettableThread
@RequiredArgsConstructor
public class CommitDiffCtxParentBubble implements CommitDiffAssertBubble {

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffCtxParentBubbleNode;
    }

    @Override
    public boolean isLeafNode() {
        return false;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(CommitDiffAssertParentCtx.class);
    }
}
