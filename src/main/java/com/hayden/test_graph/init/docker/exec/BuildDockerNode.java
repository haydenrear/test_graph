package com.hayden.test_graph.init.docker.exec;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.docker.compose.core.DockerComposeFile;
import org.springframework.boot.docker.compose.core.ExposeCompose;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@ResettableThread
@Slf4j
public class BuildDockerNode implements DockerInitNode {


    @Override
    public DockerInitCtx exec(DockerInitCtx c, MetaCtx h) {
        Optional.ofNullable(c.getGradleTasks())
                .stream().flatMap(Collection::stream).forEach(gt -> {
            var pb = new ProcessBuilder();
            try {
                pb.directory(new File(gt.gradleCommand()))
                        .command(gt.directory().split(" "))
                        .redirectOutput(new File("/Users/hayde/IdeaProjects/drools/test_graph/out.log"))
                        .start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return c;
    }

    @Override
    public Class<DockerInitCtx> clzz() {
        return DockerInitCtx.class;
    }
}
