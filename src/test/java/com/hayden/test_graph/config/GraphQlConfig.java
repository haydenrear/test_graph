package com.hayden.test_graph.config;

import com.netflix.graphql.dgs.DgsQueryExecutorConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DgsQueryExecutorConfig.class)
public class GraphQlConfig {


}
