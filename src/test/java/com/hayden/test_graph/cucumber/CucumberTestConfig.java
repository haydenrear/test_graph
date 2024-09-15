package com.hayden.test_graph.cucumber;

import com.hayden.test_graph.commit_diff_context.config.CommitDiffContextConfigProps;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.hayden.test_graph")
@EnableConfigurationProperties(CommitDiffContextConfigProps.class)
public class CucumberTestConfig {
}
