package com.hayden.test_graph.multi_agent_ide.edges;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepCtx;
import com.hayden.test_graph.multi_agent_ide.data_dep.nodes.MultiAgentIdeDataDepNode;
import com.hayden.test_graph.multi_agent_ide.init.ctx.MultiAgentIdeInit;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Edge transferring MultiAgentIdeInit context to MultiAgentIdeDataDep context.
 * This edge is used by DataDep nodes that depend on initialization configuration.
 */
@Component
@ResettableThread
public class InitToDataDepEdge implements MultiAgentIdeDataDepNode {

    @Autowired
    @ResettableThread
    private MultiAgentIdeInit initContext;

    @Override
    public MultiAgentIdeDataDepCtx exec(MultiAgentIdeDataDepCtx c, TestGraphContext h) {
        // Transfer initialization configuration to data dependency context
        if (initContext != null) {
            if (initContext.getDockerComposeConfig() != null) {
                c.putTestData("dockerComposeConfig", initContext.getDockerComposeConfig());
            }
            if (initContext.getEventSubscriptionConfig() != null) {
                c.putTestData("eventSubscriptionConfig", initContext.getEventSubscriptionConfig());
            }
            if (initContext.getGitRepositoryConfig() != null) {
                c.putTestData("gitRepositoryConfig", initContext.getGitRepositoryConfig());
            }
        }
        return c;
    }
}
