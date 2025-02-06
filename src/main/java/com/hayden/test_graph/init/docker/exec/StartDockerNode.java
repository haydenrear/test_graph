package com.hayden.test_graph.init.docker.exec;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.init.docker.config.DockerInitConfigProps;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.result.Result;
import com.hayden.utilitymodule.sort.GraphSort;
import com.hayden.utilitymodule.waiter.AsyncWaiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.boot.docker.compose.core.DockerComposeFile;
import org.springframework.boot.docker.compose.core.ExposeCompose;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@Component
@ResettableThread
@Slf4j
public class StartDockerNode implements DockerInitNode {

    private final DockerInitConfigProps dockerInitConfigProps;

    private final Assertions assertions;

    public static void initializeDockerCompose(DockerInitCtx workingDirectory,
                                               DockerInitConfigProps configProps) {
        File workDir = workingDirectory.composePath().res().one().get();
        ExposeCompose exposeCompose = new ExposeCompose(
                workDir,
                DockerComposeFile.find(workDir),
                workingDirectory.dockerProfiles().res().one().orElseRes(new HashSet<>()),
                workingDirectory.host().res().one().orElseRes(configProps.getHost()));
        exposeCompose.down(Duration.ofSeconds(10));
        exposeCompose.up(workingDirectory.logLevel().res().orElseRes(LogLevel.INFO));
    }

    @Override
    public DockerInitCtx exec(DockerInitCtx c, MetaCtx h) {
        c.composePath()
                .optional()
                .ifPresentOrElse(
                        p -> initializeDockerCompose(c, dockerInitConfigProps),
                        () -> log.info("Skipping initialization of docker compose as file was not set."));
//        Result.tryFrom(() -> DockerClientBuilder.getInstance().build())
//                .ifPresent(dc -> awaitLogMessage(".*Tomcat started on port.*", dc,
//                        "commit-diff-server"));
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

    public void awaitLogMessage(@Language("regexp") String regex,
                                DockerClient dockerClient,
                                String containerName) {
        LogContainerCmd logCmd = dockerClient.logContainerCmd(containerName);
        logCmd.withStdOut(true);
        logCmd.withStdErr(true);
        logCmd.withTailAll();

        AtomicBoolean done = new AtomicBoolean(false);

        logCmd.exec(new ResultCallback<Frame>() {
            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onNext(Frame frame) {
                String logLine = new String(frame.getPayload());
                if (logLine.matches(regex)) {
                    done.set(true);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void close() {

            }
        });


        assertions.assertSoftly(Boolean.TRUE.equals(AsyncWaiter.Builder.doCallWaiter(done::get, Boolean::valueOf)), "Could not wait");

    }

}
