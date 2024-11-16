package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitMeta;
import com.hayden.test_graph.init.mountebank.ctx.MbInitBubbleCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ResettableThread
@RequiredArgsConstructor
public class CdMbInitBubbleCtx implements MbInitBubbleCtx {

    private InitMeta initMeta;

    @Autowired
    public void setInitMeta(InitMeta initMeta) {
        this.initMeta = initMeta;
        this.initMeta.setBubbled(this);
    }

    @Override
    public InitMeta bubble() {
        return this.initMeta;
    }

    @Override
    public Class<? extends MetaCtx> bubbleClazz() {
        return InitMeta.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CdMbInitBubbleNode;
    }
}
