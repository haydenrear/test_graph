package com.hayden.test_graph.commit_diff_context.data_dep.indexing.ctx;

import com.hayden.test_graph.commit_diff_context.data_dep.indexing.CommitDiffContextIndexingDataDepBubbleNode;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.commit_diff_context.init.k3s.ctx.K3sBubble;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.sort.GraphSort;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ResettableThread
@RequiredArgsConstructor
public class CommitDiffContextIndexingDataDepBubble implements DataDepBubble {

    @Getter
    private final ContextValue<CommitDiffContextIndexingDataDepCtx> indexingCtx;

    public CommitDiffContextIndexingDataDepBubble() {
        this(ContextValue.empty());
    }

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
