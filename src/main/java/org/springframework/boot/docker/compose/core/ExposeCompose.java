package org.springframework.boot.docker.compose.core;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.io.File;
import java.util.Set;

@RequiredArgsConstructor
public class ExposeCompose {

    private final File workingDirectory;
    private final DockerComposeFile dockerComposeFile;
    private final Set<String> profiles;

    @Delegate
    DefaultDockerCompose defaultDockerCompose;



}
