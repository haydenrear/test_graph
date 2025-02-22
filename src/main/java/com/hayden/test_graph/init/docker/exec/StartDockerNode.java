package com.hayden.test_graph.init.docker.exec;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import com.hayden.commitdiffmodel.config.CommitDiffContextProperties;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.config.EnvConfigProps;
import com.hayden.test_graph.init.docker.config.DockerInitConfigProps;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.io.FileUtils;
import com.hayden.utilitymodule.result.Result;
import com.hayden.utilitymodule.result.error.SingleError;
import com.hayden.utilitymodule.waiter.AsyncWaiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.docker.compose.core.DockerComposeFile;
import org.springframework.boot.docker.compose.core.ExposeCompose;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
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

    private final EnvConfigProps env;

    private final Assertions assertions;

    @Override
    public boolean skip(DockerInitCtx initCtx) {
        if (dockerInitConfigProps.isSkipStartDocker()) {
            initCtx.getStarted().swap(true);
        }
        return dockerInitConfigProps.isSkipStartDocker();
    }

    @Override
    public DockerInitCtx exec(DockerInitCtx c, MetaCtx h) {
        c.composePath()
                .optional()
                .ifPresentOrElse(
                        p -> initializeDockerCompose(c, dockerInitConfigProps),
                        () -> log.info("Skipping initialization of docker compose as file was not set."));
        Result.tryFrom(() -> {
                    return DockerClientBuilder.getInstance()
                            .withDockerHttpClient(new ZerodepDockerHttpClient.Builder().dockerHost(URI.create(dockerInitConfigProps.getDockerHostUri()))
                                    .responseTimeout(Duration.ofSeconds(dockerInitConfigProps.getDockerResponseTimeout())).build())
                            .build();
                })
                .exceptEmpty(exc -> assertions.assertSoftly(false, "Failed to retrieve docker client for waiting for container to start: %s", exc.getMessage()))
                .ifPresent((DockerClient dc) -> dockerInitConfigProps.getContainers()
                        .forEach(container -> awaitLogMessage(container.log(), dc, container.containerName())));


        c.getStarted().swap(true);
        return c;
    }

    void initializeDockerCompose(DockerInitCtx workingDirectory,
                                        DockerInitConfigProps configProps) {
        bringDownAllComposeInDirectories(workingDirectory, configProps);
        File workDir = workingDirectory.composePath().res().one().get();
        ExposeCompose exposeCompose = new ExposeCompose(
                workDir,
                DockerComposeFile.find(workDir),
                workingDirectory.dockerProfiles().res().one().orElseRes(new HashSet<>()),
                workingDirectory.host().res().one().orElseRes(configProps.getHost()));
        exposeCompose.up(workingDirectory.logLevel().res().orElseRes(LogLevel.INFO));
    }

    private void bringDownAllComposeInDirectories(DockerInitCtx workingDirectory, DockerInitConfigProps configProps) {
        configProps.getComposeDirectories()
                .forEach(s -> FileUtils.doOnFilesRecursiveParallel(
                        FileUtils.replaceHomeDir(env.getHomeDir(), s).toPath(),
                        next -> stopNext(workingDirectory, configProps, next)));
    }

    private static @NotNull Boolean stopNext(DockerInitCtx workingDirectory, DockerInitConfigProps configProps, Path next) {
        if (!next.toFile().exists())
            return false;
        if (next.toFile().getName().equals("docker-compose.yml")) {
            new ExposeCompose(
                        next.getParent().toFile(),
                        DockerComposeFile.find(next.getParent().toFile()),
                        new HashSet<>(),
                        workingDirectory.host().res().one().orElseRes(configProps.getHost())
                    )
                    .down(Duration.ofSeconds(10));
        }

        return true;
    }

    public void awaitLogMessage(String containerLogMatch,
                                DockerClient dockerClient,
                                String containerName) {
        dockerClient.listContainersCmd()
                .exec().stream()
                .filter(c -> Arrays.stream(c.getNames()).anyMatch(n -> n.contains(containerName)))
                .map(Container::getId)
                .findAny()
                .ifPresent(containerId -> {
                    AtomicBoolean done = new AtomicBoolean(false);
                    assertions.assertSoftly(
                            Boolean.TRUE.equals(
                                    AsyncWaiter.Builder.doCallWaiter(
                                            () -> {
                                                LogContainerCmd logCmd = dockerClient.logContainerCmd(containerId);
                                                logCmd = logCmd.withStdOut(true);
                                                execCmd(containerLogMatch, containerName, logCmd, done);
                                                return done.get();
                                            },
                                            Boolean::valueOf,
                                            Duration.ofSeconds(200),
                                            Duration.ofSeconds(3))
                            ),
                            "Could not wait for container %s to start with log %s.".formatted(containerId, containerLogMatch));
                });

    }

    private void execCmd(String toMatch, String containerName, LogContainerCmd logCmd, AtomicBoolean done) {
        logCmd.exec(new ResultCallback<Frame>() {
                    @Override
                    public synchronized void onStart(Closeable closeable) {
                        log.info("Starting container '{}'", containerName);
                    }

                    @Override
                    public synchronized void onNext(Frame frame) {
                        String logLine = new String(frame.getPayload());
                        if (logLine.contains(toMatch)) {
                            done.set(true);
                            assertions.assertSoftly(true, "Container started successfully - found log: {}", logLine);
                        }
                    }

                    @Override
                    public synchronized void onError(Throwable throwable) {
                        assertions.reportAssert("Error when starting container %s, %s.", containerName,
                                SingleError.parseStackTraceToString(throwable));
                    }

                    @Override
                    public synchronized void onComplete() {
                    }

                    @Override
                    public synchronized void close() {
                    }
                });
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
