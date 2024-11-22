package com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op;

import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssertBubble;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@ResettableThread
@RequiredArgsConstructor
public class RepoOpAssertBubble implements CommitDiffAssertBubble {


    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof RepoOpAssertBubbleNode;
    }

}
