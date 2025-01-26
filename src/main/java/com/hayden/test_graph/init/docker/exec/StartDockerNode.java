package com.hayden.test_graph.init.docker.exec;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.init.docker.config.DockerInitConfigProps;
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
import java.time.Duration;
import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@Component
@ResettableThread
@Slf4j
public class StartDockerNode implements DockerInitNode {

    private final DockerInitConfigProps dockerInitConfigProps;

    public static void initializeDockerCompose(DockerInitCtx workingDirectory,
                                               DockerInitConfigProps configProps) {
        File workDir = workingDirectory.composePath().res().one().get();
        ExposeCompose exposeCompose = new ExposeCompose(
                workDir,
                DockerComposeFile.find(workDir),
                workingDirectory.dockerProfiles().res().one().orElseRes(new HashSet<>()),
                workingDirectory.host().res().one().orElseRes(configProps.getHost()));
//        exposeCompose.down(Duration.ofSeconds(10));
        exposeCompose.up(workingDirectory.logLevel().res().orElseRes(LogLevel.INFO));
        // TODO: this should be able to wait until it sees a certain log...
    }


    @Override
    public DockerInitCtx exec(DockerInitCtx c, MetaCtx h) {
        c.composePath()
                .optional()
                .ifPresentOrElse(
                        p -> initializeDockerCompose(c, dockerInitConfigProps),
                        () -> log.info("Skipping initialization of docker compose as file was not set."));
        return c;
    }

    @Override
    public List<Class<? extends DockerInitNode>> dependsOn() {
        return List.of();
    }

    @Override
    public Class<DockerInitCtx> clzz() {
        return DockerInitCtx.class;
    }
}
