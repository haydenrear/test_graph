package com.hayden.test_graph.init.docker.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.docker.exec.DockerInitNode;
import com.hayden.test_graph.init.docker.exec.StartDockerNode;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;
import java.util.Set;

@Component
@ResettableThread
@RequiredArgsConstructor
public class DockerInitCtx implements InitCtx {
    private final ContextValue<File> composePath;
    private final ContextValue<LogLevel> logLevel;
    private final ContextValue<Set<String>> dockerProfiles;
    private final ContextValue<String> host;

    private DockerInitBubbleCtx dockerInitBubbleCtx;

    @Autowired
    public void setDockerInitBubbleCtx(DockerInitBubbleCtx dockerInitBubbleCtx) {
        this.dockerInitBubbleCtx = dockerInitBubbleCtx;
    }

    public DockerInitCtx() {
        this(ContextValue.empty(), ContextValue.empty(),
                ContextValue.empty(), ContextValue.empty());
    }

    @Override
    public DockerInitBubbleCtx bubble() {
        return dockerInitBubbleCtx;
    }

    @Override
    public Class<DockerInitBubbleCtx> bubbleClazz() {
        return DockerInitBubbleCtx.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof DockerInitNode;
    }

    public ContextValue<File> composePath() {
        return composePath;
    }

    public ContextValue<LogLevel> logLevel() {
        return logLevel;
    }

    public ContextValue<Set<String>> dockerProfiles() {
        return dockerProfiles;
    }

    public ContextValue<String> host() {
        return host;
    }


}
