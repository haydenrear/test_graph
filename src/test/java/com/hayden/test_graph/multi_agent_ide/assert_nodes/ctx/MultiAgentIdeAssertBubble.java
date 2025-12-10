package com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.nodes.MultiAgentIdeAssertBubbleNode;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bubble context for multi-agent-ide assertion phase.
 * This is the final phase where test assertions are validated.
 */
@Component
@ResettableThread
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class MultiAgentIdeAssertBubble implements AssertBubble {

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof MultiAgentIdeAssertBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(MultiAgentIdeAssertCtx.class);
    }

    @Override
    public List<Class<? extends TestGraphContext<AssertBubble>>> dependsOn() {
        return List.of();
    }
}
