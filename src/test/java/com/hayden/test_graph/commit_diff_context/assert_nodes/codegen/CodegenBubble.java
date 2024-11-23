package com.hayden.test_graph.commit_diff_context.assert_nodes.codegen;

import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssertBubble;
import com.hayden.test_graph.commit_diff_context.assert_nodes.parent.CommitDiffCtxParentBubble;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Component
@ResettableThread
@RequiredArgsConstructor
public class CodegenBubble implements CommitDiffAssertBubble {

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CodegenAssertBubbleNode;
    }

    @Override
    public Optional<Class<? extends TestGraphContext>> parentTy() {
        return Optional.of(CommitDiffCtxParentBubble.class);
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(Codegen.class);
    }
}
