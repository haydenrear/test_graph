package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.mountebank.ctx.MbInitBubbleCtx;
import com.hayden.test_graph.init.mountebank.ctx.MbInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ResettableThread
@RequiredArgsConstructor
public class CdMbInitBubbleCtx implements MbInitBubbleCtx {


    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CdMbInitBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(CdMbInitCtx.class);
    }
}
