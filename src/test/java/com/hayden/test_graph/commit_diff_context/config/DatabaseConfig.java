package com.hayden.test_graph.commit_diff_context.config;

import com.hayden.persistence.config.QueryDslConfig;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Import(QueryDslConfig.class)
@EnableJpaRepositories(basePackages = {"com.hayden.commitdiffcontext"})
@ComponentScan(basePackages = "com.hayden.commitdiffcontext")
@EntityScan(basePackages = "com.hayden.commitdiffcontext")
public class DatabaseConfig {
}
