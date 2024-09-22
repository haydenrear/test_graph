package com.hayden.test_graph.init.docker.ctx;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitMeta;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.stereotype.Component;

@ThreadScope
@Component
public class DockerInitBubbleCtx implements InitBubble {
    @Override
    public InitMeta bubble() {
        return new InitMeta(this);
    }

    @Override
    public Class<InitMeta> bubbleClazz() {
        return InitMeta.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return false;
    }

}
