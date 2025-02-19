package com.hayden.test_graph.init.docker.ctx;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.docker.config.DockerInitConfigProps;
import com.hayden.test_graph.init.docker.exec.DockerInitBubbleNode;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@ResettableThread
@Component
public class DockerInitBubbleCtx implements InitBubble {


    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof DockerInitBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(DockerInitCtx.class);
    }
}
