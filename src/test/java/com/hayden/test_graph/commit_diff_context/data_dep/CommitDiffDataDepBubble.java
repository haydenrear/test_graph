package com.hayden.test_graph.commit_diff_context.data_dep;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ResettableThread
@RequiredArgsConstructor
public class CommitDiffDataDepBubble implements DataDepBubble {


    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffDataDepBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(CommitDiffDataDepCtx.class);
    }
}
