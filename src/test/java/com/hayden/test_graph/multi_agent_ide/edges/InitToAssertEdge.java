package com.hayden.test_graph.multi_agent_ide.edges;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx.MultiAgentIdeAssertCtx;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.nodes.MultiAgentIdeAssertNode;
import com.hayden.test_graph.multi_agent_ide.init.ctx.MultiAgentIdeInit;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Edge transferring MultiAgentIdeInit context to MultiAgentIdeAssert context.
 * This edge is used by Assert nodes that need to access initialization configuration.
 */
@Component
@ResettableThread
public class InitToAssertEdge implements MultiAgentIdeAssertNode {

    @Autowired
    @ResettableThread
    private MultiAgentIdeInit initContext;

    @Override
    public MultiAgentIdeAssertCtx exec(MultiAgentIdeAssertCtx c, MetaCtx h) {
        // Transfer initialization configuration to assert context
        c.setInitContext(initContext);
        return c;
    }
}
