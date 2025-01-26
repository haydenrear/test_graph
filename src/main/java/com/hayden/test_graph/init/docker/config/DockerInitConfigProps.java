package com.hayden.test_graph.init.docker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "docker-init-ctx")
@Component
@Data
public class DockerInitConfigProps {

    String host;

}
