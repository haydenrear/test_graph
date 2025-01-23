package org.springframework.boot.docker.compose.core;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.io.File;
import java.util.Set;

public class ExposeCompose {


    public ExposeCompose(File workingDirectory, DockerComposeFile dockerComposeFile, Set<String> profiles, String host) {
        this.defaultDockerCompose = new DefaultDockerCompose(new DockerCli(workingDirectory, dockerComposeFile, profiles), host);
    }

    @Delegate
    DefaultDockerCompose defaultDockerCompose;



}
