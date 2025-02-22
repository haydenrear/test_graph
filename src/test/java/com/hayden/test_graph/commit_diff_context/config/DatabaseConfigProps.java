package com.hayden.test_graph.commit_diff_context.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "db-init-ctx")
@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseConfigProps {

    boolean skipDbCleanup;

}
