package com.hayden.test_graph.commit_diff_context.assert_nodes.next_commit;

import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssertBubble;
import com.hayden.test_graph.commit_diff_context.assert_nodes.parent.CommitDiffCtxParentBubble;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.sort.GraphSort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Component
@ResettableThread
@RequiredArgsConstructor
public class NextCommitBubble implements CommitDiffAssertBubble {

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof NextCommitAssertBubbleNode;
    }

    @Override
    public Optional<Class<? extends TestGraphContext>> parentTy() {
        return Optional.of(CommitDiffCtxParentBubble.class);
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(NextCommitAssert.class);
    }

}
