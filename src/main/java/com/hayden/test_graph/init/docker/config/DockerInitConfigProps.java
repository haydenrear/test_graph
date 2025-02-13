package com.hayden.test_graph.init.docker.config;

import lombok.Data;
import org.intellij.lang.annotations.Language;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "docker-init-ctx")
@Component
@Data
public class DockerInitConfigProps {

    public record AwaitableContainer(String containerName, String log) {}

    String host;

    List<AwaitableContainer> containers = new ArrayList<>();


}
