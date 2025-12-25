package com.hayden.test_graph.commit_diff_context.config;

import com.hayden.commitdiffmodel.config.CommitDiffContextConfigProps;
import com.netflix.graphql.dgs.DgsQueryExecutorConfig;
import com.netflix.graphql.dgs.GraphQlClientProps;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.client.DgsGraphQlClient;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@Import(DgsQueryExecutorConfig.class)
public class GraphQlConfig {


}
