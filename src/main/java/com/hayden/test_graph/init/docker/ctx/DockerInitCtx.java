package com.hayden.test_graph.init.docker.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.docker.exec.StartDockerNode;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Set;

@Component
@ResettableThread
public record DockerInitCtx(ContextValue<File> composePath,
                            ContextValue<LogLevel> logLevel,
                            ContextValue<Set<String>> dockerProfiles,
                            ContextValue<String> host) implements InitCtx {

    public DockerInitCtx() {
        this(ContextValue.empty(), ContextValue.empty(),
                ContextValue.empty(), ContextValue.empty());
    }

    @Override
    public DockerInitBubbleCtx bubble() {
        return new DockerInitBubbleCtx();
    }

    @Override
    public Class<DockerInitBubbleCtx> bubbleClazz() {
        return DockerInitBubbleCtx.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof StartDockerNode;
    }

}
