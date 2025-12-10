package com.hayden.test_graph.multi_agent_ide.data_dep.nodes;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepCtx;

/**
 * Marker interface for multi-agent-ide data dependency nodes.
 * These nodes execute during the data dependency phase to set up test data and configuration.
 */
public interface MultiAgentIdeDataDepNode extends GraphExec.GraphExecNode {
    
    MultiAgentIdeDataDepCtx exec(MultiAgentIdeDataDepCtx c, TestGraphContext h);
}
