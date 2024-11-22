package com.hayden.test_graph.init.docker.ctx;

import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.docker.exec.DockerInitBubbleNode;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ResettableThread
@Component
public class DockerInitBubbleCtx implements InitBubble {

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof DockerInitBubbleNode;
    }

}
