package com.hayden.test_graph.multi_agent_ide.edges;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx.MultiAgentIdeAssertCtx;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.nodes.MultiAgentIdeAssertNode;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Edge transferring MultiAgentIdeDataDep context to MultiAgentIdeAssert context.
 * This edge is used by Assert nodes that depend on data dependency setup.
 */
@Component
@ResettableThread
public class DataDepToAssertEdge implements MultiAgentIdeAssertNode {

    @Autowired
    @ResettableThread
    private MultiAgentIdeDataDepCtx dataDepContext;

    @Override
    public MultiAgentIdeAssertCtx exec(MultiAgentIdeAssertCtx c, MetaCtx h) {
        // Transfer data dependency configuration to assert context
        c.setDataDepContext(dataDepContext);
        c.setUiEvents(dataDepContext.getUiEvents());
        
        // Copy test data from data dep context
        if (dataDepContext.getEventListenerConfig() != null) {
            c.putAssertionResult("eventListenerConfig", dataDepContext.getEventListenerConfig());
        }
        if (dataDepContext.getLangChain4jMockConfig() != null) {
            c.putAssertionResult("langChain4jMockConfig", dataDepContext.getLangChain4jMockConfig());
        }
        if (dataDepContext.getExpectedEventCount() != null) {
            c.putAssertionResult("expectedEventCount", dataDepContext.getExpectedEventCount());
        }
        
        return c;
    }
}
