package com.hayden.test_graph.multi_agent_ide.data_dep.ctx;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.multi_agent_ide.data_dep.nodes.MultiAgentIdeDataDepBubbleNode;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bubble context for multi-agent-ide data dependency phase.
 * Depends on no other bubbles as multi-agent-ide is self-contained.
 */
@Component
@ResettableThread
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class MultiAgentIdeDataDepBubble implements DataDepBubble {

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof MultiAgentIdeDataDepBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(MultiAgentIdeDataDepCtx.class);
    }

}
