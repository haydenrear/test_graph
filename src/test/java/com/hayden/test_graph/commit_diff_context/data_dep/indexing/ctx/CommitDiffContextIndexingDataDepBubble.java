package com.hayden.test_graph.commit_diff_context.data_dep.indexing.ctx;

import com.hayden.test_graph.commit_diff_context.data_dep.indexing.CommitDiffContextIndexingDataDepBubbleNode;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
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
public class CommitDiffContextIndexingDataDepBubble implements DataDepBubble {

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffContextIndexingDataDepBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(CommitDiffContextIndexingDataDepCtx.class);
    }

    @Override
    public List<Class<? extends HyperGraphContext>> dependsOn() {
        return DataDepBubble.super.dependsOn();
    }
}
