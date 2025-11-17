package com.hayden.test_graph.commit_diff_context.init.mountebank.indexing.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.commit_diff_context.init.mountebank.indexing.IndexingMbInitBubbleNode;
import com.hayden.test_graph.init.mountebank.ctx.MbInitBubbleCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ResettableThread
@RequiredArgsConstructor
public class IndexingMbInitBubbleCtx implements MbInitBubbleCtx {

    @Getter
    private final ContextValue<IndexingMbInitCtx> indexingMbInit;

    public IndexingMbInitBubbleCtx() {
        this(ContextValue.empty());
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof IndexingMbInitBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(IndexingMbInitCtx.class);
    }
}
