package org.springframework.boot.docker.compose.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class ExposeCompose {


    public ExposeCompose(File workingDirectory, DockerComposeFile dockerComposeFile,
                         Set<String> profiles, String host) {
        this.defaultDockerCompose = new DefaultDockerCompose(new DockerCli(workingDirectory,
                new DockerCli.DockerComposeOptions(dockerComposeFile, profiles, new ArrayList<>())), host);
    }


    @Delegate
    DefaultDockerCompose defaultDockerCompose;



}
