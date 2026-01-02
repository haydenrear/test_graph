package com.hayden.test_graph.multi_agent_ide.init.ctx;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.docker.ctx.DockerInitBubbleCtx;
import com.hayden.test_graph.init.selenium.ctx.SeleniumInitBubbleCtx;
import com.hayden.test_graph.multi_agent_ide.init.nodes.MultiAgentIdeInitBubbleNode;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bubble context for multi-agent-ide initialization phase.
 * Depends on DockerInitBubbleCtx to ensure docker-compose is started first.
 */
@Component
@ResettableThread
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class MultiAgentIdeBubble implements InitBubble {

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof MultiAgentIdeInitBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(MultiAgentIdeInit.class);
    }

    @Override
    public List<Class<? extends TestGraphContext<InitBubble>>> dependsOn() {
        // Multi-agent-ide initialization depends on Docker being ready
        return List.of(DockerInitBubbleCtx.class, SeleniumInitBubbleCtx.class);
    }
}
