package com.hayden.test_graph.commit_diff_context.config;

import com.hayden.commitdiffmodel.config.CommitDiffContextConfigProps;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.DgsGraphQlClient;
import org.springframework.graphql.client.HttpSyncGraphQlClient;

@Configuration
public class GraphQlConfig {

    @Bean
    public DgsGraphQlClient graphQlClient(
            CommitDiffContextConfigProps commitDiffContextConfigProps
    ) {
        return DgsGraphQlClient.create(
                HttpSyncGraphQlClient.builder()
                        .url(commitDiffContextConfigProps.getUrl())
                        .build());
    }

}
