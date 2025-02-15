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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@Component
@ResettableThread
@Slf4j
public class BuildDockerNode implements DockerInitNode {

    private final DockerInitConfigProps dockerInitConfigProps;

    private final Assertions assertions;

    @Override
    public DockerInitCtx exec(DockerInitCtx c, MetaCtx h) {
//        Result.tryFrom(() -> {
//                    return DockerClientBuilder.getInstance()
//                            .withDockerHttpClient(new ZerodepDockerHttpClient.Builder().dockerHost(URI.create(dockerInitConfigProps.getDockerHostUri()))
//                                    .responseTimeout(Duration.ofSeconds(dockerInitConfigProps.getDockerResponseTimeout())).build())
//                            .build();
//                })
//                .exceptEmpty(exc -> assertions.assertSoftly(false, "Failed to retrieve docker client for waiting for container to start: %s", exc.getMessage()))
//                .ifPresent((DockerClient dc) -> {
//                    var allImages = dc.listImagesCmd().withShowAll(true).exec();
//                    for (var toAsserImage : c.getContainers()) {
//                        if (allImages.stream()
//                                .noneMatch(img -> Arrays.stream(img.getRepoTags())
//                                    .anyMatch(tag -> Objects.equals(toAsserImage.imageName(), tag)))) {
//                            assertions.assertSoftly(false, "%s image did not exist in Docker.".formatted(toAsserImage.imageName()));
//                        } else {
//                            assertions.assertSoftly(true, "%s image existed.".formatted(toAsserImage.imageName()));
//                        }
//                    }
//                });


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
