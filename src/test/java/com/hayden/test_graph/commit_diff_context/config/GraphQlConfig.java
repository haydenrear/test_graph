package com.hayden.test_graph.commit_diff_context.config;

import com.hayden.commitdiffmodel.config.CommitDiffContextConfigProps;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.DgsGraphQlClient;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class GraphQlConfig {

    @Bean
    public DgsGraphQlClient graphQlClient(
            CommitDiffContextConfigProps commitDiffContextConfigProps
    ) {
        var template = new RestTemplateBuilder();
        template = template.connectTimeout(Duration.ofSeconds(1000));
        template = template.readTimeout(Duration.ofSeconds(1000));
        return DgsGraphQlClient.create(
                HttpSyncGraphQlClient.builder(RestClient.create(template.build()).mutate().baseUrl(commitDiffContextConfigProps.getUrl()).build())
                        .blockingTimeout(Duration.ofSeconds(1000))
                        .url(commitDiffContextConfigProps.getUrl())
                        .build());
    }

}
