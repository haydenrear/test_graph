package com.hayden.test_graph.init.docker.exec;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.init.exec.single.InitNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.docker.compose.core.DockerComposeFile;
import org.springframework.boot.docker.compose.core.ExposeCompose;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashSet;

@RequiredArgsConstructor
@Component
@ThreadScope
@Slf4j
public class StartDockerNode implements DockerInitNode {



    public static void initializeDockerCompose(File workingDirectory) {
//        ExposeCompose exposeCompose = new ExposeCompose(workingDirectory, DockerComposeFile.find(workingDirectory), new HashSet<>());
//        exposeCompose.up(LogLevel.DEBUG);
    }

    @Override
    @Idempotent
    public DockerInitCtx exec(DockerInitCtx c, MetaCtx h) {
        var composeFile = c.composePath().optional()
                .orElseThrow(() -> new RuntimeException("Could not initialize docker compose, as compose file not provided."));
        log.info("Initializing docker compose.");
        initializeDockerCompose(composeFile);
        return c;
    }

    @Override
    public Class<DockerInitCtx> clzz() {
        return DockerInitCtx.class;
    }
}
