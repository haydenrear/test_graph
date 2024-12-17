package com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx;

import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.CommitDiffInitNode;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;

@Component
@ResettableThread
@RequiredArgsConstructor
public final class CommitDiffInit implements InitCtx {

    private final ContextValue<CommitDiffInitBubble> bubbleUnderlying;

    public CommitDiffInit() {
        this(ContextValue.empty());
    }

    @Autowired
    public void setBubble(CommitDiffInitBubble bubble) {
        this.bubbleUnderlying.set(bubble);
    }


    @Override
    public CommitDiffInitBubble bubble() {
        return this.bubbleUnderlying.res().one().get();
    }

    @Override
    public Class<? extends InitBubble> bubbleClazz() {
        return CommitDiffInitBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffInitNode;
    }
}

