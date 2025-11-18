package com.hayden.test_graph.init.k3s.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.k3s.config.K3sInitConfigProps;
import com.hayden.test_graph.init.k3s.exec.K3sInitNode;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@ResettableThread
@RequiredArgsConstructor
public class K3sInitCtx implements InitCtx {

    @Getter
    private final ContextValue<Boolean> started;

    @Getter
    private final List<AssertContainer> containers = new ArrayList<>();


    public record AssertContainer(String imageName) {}

    private K3sInitBubbleCtx k3sInitBubbleCtx;

    @Autowired
    private K3sInitConfigProps k3sInitConfigProps;

    @Override
    public boolean skip() {
        if (k3sInitConfigProps.isSkipK3s()) {
            getStarted().swap(true);
        }
        return k3sInitConfigProps.isSkipK3s();
    }

    @Autowired
    public void setK3sInitBubbleCtx(K3sInitBubbleCtx k3sInitBubbleCtx) {
        this.k3sInitBubbleCtx = k3sInitBubbleCtx;
    }

    public K3sInitCtx() {
        this(ContextValue.empty());
    }

    @Override
    public K3sInitBubbleCtx bubble() {
        return k3sInitBubbleCtx;
    }

    @Override
    public Class<K3sInitBubbleCtx> bubbleClazz() {
        return K3sInitBubbleCtx.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof K3sInitNode;
    }

}
