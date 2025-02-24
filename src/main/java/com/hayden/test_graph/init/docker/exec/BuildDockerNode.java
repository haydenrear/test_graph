package com.hayden.test_graph.init.docker.exec;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.init.docker.DockerService;
import com.hayden.test_graph.init.docker.config.DockerInitConfigProps;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.MapFunctions;
import com.hayden.utilitymodule.git.RepoUtil;
import com.hayden.utilitymodule.result.Result;
import com.hayden.utilitymodule.result.error.SingleError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Sets;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
@ResettableThread
@Slf4j
public class BuildDockerNode implements DockerInitNode {

    private final DockerInitConfigProps dockerInitConfigProps;

    private final Assertions assertions;

    private final DockerService dockerService;

    @Override
    public boolean skip(DockerInitCtx dockerInitCtx) {
        return dockerInitConfigProps.isSkipBuildDocker();
    }

    @Override
    public DockerInitCtx exec(DockerInitCtx c, MetaCtx h) {
        Result.tryFrom(dockerService::buildDockerClient)
                .exceptEmpty(exc -> assertions.assertSoftly(false, "Failed to retrieve docker client for waiting for container to start: %s", exc.getMessage()))
                .ifPresent((DockerClient dc) -> doPerformBuilds(c, dc));

        return c;
    }

    private void doPerformBuilds(DockerInitCtx c, DockerClient dc) {
        var allImages = dc.listImagesCmd().withShowAll(true).exec();
        var images = toBuildImages(c);
        for (var toAssertImageExists : c.getContainers()) {
            if (allImages.stream()
                    .anyMatch(img -> Arrays.stream(img.getRepoTags())
                            .noneMatch(tag -> Objects.equals(toAssertImageExists.imageName(), tag)))) {
                images.compute(toAssertImageExists.imageName(), (key, prev) -> {
                    if (prev == null) {
                        assertions.assertSoftly(false, "%s image did not exist in Docker.".formatted(toAssertImageExists.imageName()));
                        return null;
                    } else {
                        doPerformDockerBuildCommands(dc, prev);
                        return prev;
                    }
                });
            } else {
                assertions.assertSoftly(true, "%s image existed.".formatted(toAssertImageExists.imageName()));
            }

            images.remove(toAssertImageExists.imageName());
        }

        images.forEach((imageName, toBuild) -> doPerformDockerBuildCommands(dc, toBuild));
    }

    private Map<String, DockerInitCtx.DockerTask.BuildCloneDockerTask> toBuildImages(DockerInitCtx initCtx) {
        return MapFunctions.CollectMap(initCtx.getDockerBuildCommands()
                .stream()
                .flatMap(dt -> dt instanceof DockerInitCtx.DockerTask.BuildCloneDockerTask t ? Stream.of(Map.entry(t.imageName(), t)) : Stream.empty()));
    }

    private void doPerformDockerBuildCommands(DockerClient dc, DockerInitCtx.DockerTask.BuildCloneDockerTask dockerTask) {
        var repoUri = dockerTask.repoUri();
        var branch = dockerTask.branch();
        var dockerfile = dockerTask.dockerfile();
        var contextPath = dockerTask.contextPath();
        var imageName = dockerTask.imageName();
        RepoUtil.doDecompressCloneRepo(repoUri, branch)
                .doOnError(repoUtilError -> assertions.assertSoftly(false, "Failed to find docker repo for building %s, %s"
                        .formatted(repoUri, repoUtilError.getMessage())))
                .ifPresent(clonedRepo -> {
                    try {
                        Path dockerPath = clonedRepo.resolve(contextPath).resolve(dockerfile);
                        var imageId = dc.buildImageCmd(dockerPath.toFile())
                                .withBaseDirectory(clonedRepo.resolve(contextPath).toFile())
                                .withDockerfile(dockerPath.toFile())
                                .withTags(Sets.newLinkedHashSet(imageName))
                                .exec(new BuildImageResultCallback())
                                .awaitCompletion()
                                .awaitImageId();
                        assertions.reportAssert("Awaited image id: %s".formatted(imageId));
                    } catch (
                            InterruptedException e) {
                        assertions.assertSoftly(false, "Failed to build docker image %s with error\n%s"
                                .formatted(dockerfile, SingleError.parseStackTraceToString(e)));
                    }
                });
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
