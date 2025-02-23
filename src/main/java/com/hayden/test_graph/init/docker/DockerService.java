package com.hayden.test_graph.init.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import com.hayden.test_graph.init.docker.config.DockerInitConfigProps;
import com.hayden.utilitymodule.concurrent.OnceCell;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DockerService {

    private final DockerInitConfigProps dockerInitConfigProps;

    private final OnceCell<DockerClient> dockerClient = new OnceCell<>(this::buildDockerClientOnce);


    public DockerClient buildDockerClient() {
        return dockerClient.get();
    }

    private DockerClient buildDockerClientOnce() {
        return DockerClientBuilder.getInstance()
                .withDockerHttpClient(new ZerodepDockerHttpClient.Builder().dockerHost(URI.create(dockerInitConfigProps.getDockerHostUri()))
                        .responseTimeout(Duration.ofSeconds(dockerInitConfigProps.getDockerResponseTimeout())).build())
                .build();
    }

}
