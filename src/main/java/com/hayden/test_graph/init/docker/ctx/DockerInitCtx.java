package com.hayden.test_graph.init.docker.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.docker.exec.StartDockerNode;
import com.hayden.test_graph.thread.ThreadScope;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;

@Data
@RequiredArgsConstructor
@Component
@ThreadScope
public class DockerInitCtx implements InitCtx {

    private final ContextValue<File> composePath = ContextValue.empty();

    @Override
    public DockerInitBubbleCtx bubble() {
        return new DockerInitBubbleCtx();
    }

    @Override
    public Class<DockerInitBubbleCtx> bubbleClazz() {
        return DockerInitBubbleCtx.class;
    }

    @Override
    public boolean executableFor(GraphNode n) {
        return n instanceof StartDockerNode;
    }

}
