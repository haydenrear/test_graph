package com.hayden.test_graph.init.selenium.ctx;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.init.docker.exec.DockerInitBubbleNode;
import com.hayden.test_graph.init.selenium.exec.SeleniumInitBubbleNode;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.stereotype.Component;

import java.util.List;

@ResettableThread
@Component
public class SeleniumInitBubbleCtx implements InitBubble {
    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof SeleniumInitBubbleNode;
    }

    @Override
    public List bubblers() {
        return List.of(SeleniumInitCtx.class);
    }
}
