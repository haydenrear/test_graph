package com.hayden.test_graph.commit_diff_context.config;

import com.hayden.utilitymodule.ctx.ApplicationContextProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.client.HttpSyncGraphQlClient;

@Configuration
@Import(ApplicationContextProvider.class)
public class LoggerConfig {

}
