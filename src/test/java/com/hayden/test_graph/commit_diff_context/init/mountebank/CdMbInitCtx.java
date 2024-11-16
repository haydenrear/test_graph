package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.mountebank.ctx.MbInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.mbtest.javabank.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// TODO: add edge and data so it sets data to call imposters
@Component
@ResettableThread
@RequiredArgsConstructor
public class CdMbInitCtx implements MbInitCtx {

    public record AiClientResponses() {}

    Client client;

    private final ContextValue<CdMbInitBubbleCtx> bubbleUnderlying;

    private final ContextValue<AiClientResponses> aiClientResponses;

    public CdMbInitCtx() {
        this(ContextValue.empty(), ContextValue.empty());
    }

    @Autowired
    public void setBubble(CdMbInitBubbleCtx bubble) {
        this.bubbleUnderlying.set(bubble);
    }

    @Override
    public Client client() {
        return this.client;
    }

    @Override
    @Autowired
    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public CdMbInitBubbleCtx bubble() {
        return this.bubbleUnderlying.res().get();
    }

    @Override
    public Class<CdMbInitBubbleCtx> bubbleClazz() {
        return CdMbInitBubbleCtx.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CdMbInitNode;
    }
}
