package com.hayden.test_graph.multi_agent_ide.init.mountebank.ctx;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.docker.ctx.DockerInitBubbleCtx;
import com.hayden.test_graph.init.mountebank.ctx.MbInitBubbleCtx;
import com.hayden.test_graph.multi_agent_ide.init.mountebank.nodes.MultiAgentIdeMbInitBubbleNode;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bubble context for multi-agent-ide mountebank initialization.
 * Depends on DockerInitBubbleCtx to ensure docker is ready before mocking.
 */
@Component
@ResettableThread
public class MultiAgentIdeMbInitBubbleCtx implements MbInitBubbleCtx {

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof MultiAgentIdeMbInitBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(MultiAgentIdeMbInitCtx.class);
    }

    @Override
    public List<Class<? extends TestGraphContext<InitBubble>>> dependsOn() {
        return List.of(DockerInitBubbleCtx.class);
    }
}
