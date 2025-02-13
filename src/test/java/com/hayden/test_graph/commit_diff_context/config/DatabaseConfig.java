package com.hayden.test_graph.commit_diff_context.config;

import com.hayden.persistence.config.QueryDslConfig;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Import(QueryDslConfig.class)
@EnableJpaRepositories(basePackages = {
        "com.hayden.commitdiffmodel.repo",
        "com.hayden.commitdiffmodel.validation.repo"})
@ComponentScan(basePackages = "com.hayden.commitdiffmodel.repo")
@EntityScan(basePackages = "com.hayden.commitdiffmodel")
public class DatabaseConfig {
}
