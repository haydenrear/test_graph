package com.hayden.test_graph.multi_agent_ide.assert_nodes.nodes;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx.MultiAgentIdeAssertCtx;

/**
 * Marker interface for multi-agent-ide assertion nodes.
 * These nodes execute during the assertion phase to validate test results.
 */
public interface MultiAgentIdeAssertNode extends GraphExec.GraphExecNode {
    
    MultiAgentIdeAssertCtx exec(MultiAgentIdeAssertCtx c, TestGraphContext h);
}
