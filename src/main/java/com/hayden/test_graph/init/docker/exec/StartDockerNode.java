package com.hayden.test_graph.init.docker.exec;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.sort.GraphSort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.docker.compose.core.DockerComposeFile;
import org.springframework.boot.docker.compose.core.ExposeCompose;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@Component
@ResettableThread
@Slf4j
public class StartDockerNode implements DockerInitNode {

    public static void initializeDockerCompose(DockerInitCtx workingDirectory) {
        File workDir = workingDirectory.composePath().res().one().get();
        ExposeCompose exposeCompose = new ExposeCompose(
                workDir,
                DockerComposeFile.find(workDir),
                workingDirectory.dockerProfiles().res().one().orElseRes(new HashSet<>()),
                workingDirectory.host().res().one().orElseRes("localhost"));
        exposeCompose.up(workingDirectory.logLevel().res().orElseRes(LogLevel.INFO));
    }


    @Override
    public DockerInitCtx exec(DockerInitCtx c, MetaCtx h) {
        c.composePath()
                .optional()
                .ifPresentOrElse(
                        p -> initializeDockerCompose(c),
                        () -> log.info("Skipping initialization of docker compose as file was not set."));
        return c;
    }

    @Override
    public List<Class<? extends DockerInitNode>> dependsOn() {
        return List.of(BuildDockerNode.class);
    }

    @Override
    public Class<DockerInitCtx> clzz() {
        return DockerInitCtx.class;
    }
}
