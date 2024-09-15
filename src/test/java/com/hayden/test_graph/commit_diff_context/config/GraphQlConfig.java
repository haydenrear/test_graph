package com.hayden.test_graph.commit_diff_context.config;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpSyncGraphQlClient;

@Configuration
public class GraphQlConfig {

    @Bean
    public HttpSyncGraphQlClient graphQlClient(
            CommitDiffContextConfigProps commitDiffContextConfigProps
    ) {
        return HttpSyncGraphQlClient.builder()
                .url(commitDiffContextConfigProps.getUrl())
                .build();
    }

}
