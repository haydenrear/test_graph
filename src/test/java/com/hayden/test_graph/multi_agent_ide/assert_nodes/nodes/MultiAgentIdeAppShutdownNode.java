package com.hayden.test_graph.multi_agent_ide.assert_nodes.nodes;

import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.multi_agent_ide.init.ctx.MultiAgentIdeInit;
import com.hayden.test_graph.thread.ResettableThread;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ResettableThread
public class MultiAgentIdeAppShutdownNode implements MultiAgentIdeAssertNode {

    @Autowired
    @ResettableThread
    private MultiAgentIdeInit initContext;

    @Override
    public com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx.MultiAgentIdeAssertCtx exec(
            com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx.MultiAgentIdeAssertCtx c,
            MetaCtx h
    ) {
        Process process = initContext.getAppProcess();
        if (process == null) {
            return c;
        }
        if (process.isAlive()) {
            process.destroy();
            log.info("Stopped multi-agent IDE process");
        }
        return c;
    }

    @Override
    public List<Class<? extends MultiAgentIdeAssertNode>> dependsOn() {
        return List.of(MultiAgentIdeUiEventAssertNode.class);
    }
}
