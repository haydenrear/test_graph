package com.hayden.test_graph.multi_agent_ide.edges;

import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx.MultiAgentIdeAssertCtx;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.nodes.MultiAgentIdeAssertNode;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Edge transferring multi-agent-ide data dependency context to the assertion phase.
 */
@Component
@ResettableThread
public class MultiAgentIdeDataDepToAssertEdge implements MultiAgentIdeAssertNode {

    private MultiAgentIdeDataDepCtx dataDepContext;

    @Autowired
    @ResettableThread
    public void setDataDepContext(MultiAgentIdeDataDepCtx dataDepContext) {
        this.dataDepContext = dataDepContext;
    }

    @Override
    public MultiAgentIdeAssertCtx exec(MultiAgentIdeAssertCtx c, MetaCtx h) {
        c.setDataDepContext(dataDepContext);
        return c;
    }

    @Override
    public List<Class<? extends MultiAgentIdeAssertNode>> dependsOn() {
        return List.of(InitToAssertEdge.class);
    }
}
