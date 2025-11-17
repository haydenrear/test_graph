package com.hayden.test_graph.commit_diff_context.init.k3s.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.docker.ctx.DockerInitBubbleCtx;
import com.hayden.test_graph.commit_diff_context.init.k3s.K3sInitBubbleNode;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ResettableThread
@RequiredArgsConstructor
public class K3sBubble implements InitBubble {

    @Getter
    private final ContextValue<K3sInit> k3sInit;

    public K3sBubble() {
        this(ContextValue.empty());
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof K3sInitBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext<InitBubble>>> dependsOn() {
        return List.of();
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(K3sInit.class);
    }
}
