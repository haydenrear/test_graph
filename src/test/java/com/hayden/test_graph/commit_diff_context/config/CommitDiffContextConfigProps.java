package com.hayden.test_graph.commit_diff_context.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "commit-diff-context")
@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitDiffContextConfigProps {

    String url;

    int port;

    String modelServerApiKey;

}
