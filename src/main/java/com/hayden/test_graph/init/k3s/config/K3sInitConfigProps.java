package com.hayden.test_graph.init.k3s.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "k3s-init-ctx")
@Component
@Data
public class K3sInitConfigProps {

    boolean skipK3s;

    boolean skipStartK3s;

    boolean skipBuildK3s;

    Path uvExecutable;

    Path pythonFile;

    List<String> options = new ArrayList<>();

}
