package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.mountebank.ctx.MbInitBubbleCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@ResettableThread
@RequiredArgsConstructor
public class CdMbInitBubbleCtx implements MbInitBubbleCtx {


    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CdMbInitBubbleNode;
    }

}
