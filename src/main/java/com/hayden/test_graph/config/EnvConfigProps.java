package com.hayden.test_graph.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "test-graph.env")
@Component
@Data
public class EnvConfigProps {

    Path homeDir;

}
