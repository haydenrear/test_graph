package com.hayden.test_graph.init.docker.exec;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.init.docker.config.DockerInitConfigProps;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.result.Result;
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
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
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
//        c.composePath()
//                .optional()
//                .ifPresentOrElse(
//                        p -> initializeDockerCompose(c, dockerInitConfigProps),
//                        () -> log.info("Skipping initialization of docker compose as file was not set."));
//        Result.tryFrom(() -> {
//                    return DockerClientBuilder.getInstance()
//                            .withDockerHttpClient(new ZerodepDockerHttpClient.Builder().dockerHost(URI.create("unix:///Users/hayde/.docker/run/docker.sock"))
//                                    .responseTimeout(Duration.ofSeconds(30)).build())
//                            .build();
//                })
//                .exceptEmpty(exc -> assertions.assertSoftly(false, "Failed to retrieve docker client for waiting for container to start: %s", exc.getMessage()))
//                .ifPresent((DockerClient dc) -> dockerInitConfigProps.getContainers()
//                        .forEach(container -> awaitLogMessage(container.log(), dc, container.containerName())));
//
        c.getStarted().swap(true);
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

    public void awaitLogMessage(String regex,
                                DockerClient dockerClient,
                                String containerName) {
        List<Container> res = dockerClient.listContainersCmd().exec();
        res.stream()
                .filter(c -> Arrays.stream(c.getNames()).anyMatch(n -> n.contains(containerName)))
                .map(Container::getId)
                .findAny()
                .ifPresent(containerId -> {

                    AtomicBoolean done = new AtomicBoolean(false);
                    assertions.assertSoftly(Boolean.TRUE.equals(AsyncWaiter.Builder.doCallWaiter(
                            () -> {
                                LogContainerCmd logCmd = dockerClient.logContainerCmd(containerId);
                                logCmd = logCmd.withStdOut(true);
                                execCmd(regex, containerName, logCmd, done);
                                return done.get();
                            },
                            Boolean::valueOf,
                            Duration.ofSeconds(120),
                            Duration.ofSeconds(3))
                    ), "Could not wait");
                });

    }

    private void execCmd(String toMatch, String containerName, LogContainerCmd logCmd, AtomicBoolean done) {
        var logFile = logCmd
                .exec(new ResultCallback<Frame>() {
                    @Override
                    public void onStart(Closeable closeable) {
                        log.info("Starting container '{}'", containerName);
                    }

                    @Override
                    public void onNext(Frame frame) {
                        String logLine = new String(frame.getPayload());
                        if (logLine.contains(toMatch)) {
                            done.set(true);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        assertions.assertSoftly(false, "Error when starting container %s.", containerName);
                    }

                    @Override
                    public void onComplete() {
                        assertions.assertSoftly(done.get(), "Could not wait for container '%s'", containerName);
                    }

                    @Override
                    public void close() {
                        assertions.assertSoftly(done.get(), "Could not wait for container '%s'", containerName);
                    }
                });
    }

}
