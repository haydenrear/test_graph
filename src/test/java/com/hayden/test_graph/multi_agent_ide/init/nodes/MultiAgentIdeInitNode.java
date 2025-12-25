package com.hayden.test_graph.multi_agent_ide.init.nodes;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.exec.single.InitNode;
import com.hayden.test_graph.multi_agent_ide.init.ctx.MultiAgentIdeInit;

/**
 * Marker interface for multi-agent-ide initialization nodes.
 * These nodes execute during the initialization phase and set up test configuration.
 */
public interface MultiAgentIdeInitNode extends InitNode<MultiAgentIdeInit> {

    @Override
    default Class<MultiAgentIdeInit> clzz() {
        return MultiAgentIdeInit.class;
    }
}
