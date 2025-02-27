package com.hayden.test_graph.cucumber;

import com.hayden.test_graph.commit_diff_context.config.CommitDiffContextConfigProps;
import com.hayden.utilitymodule.config.EnvConfigProps;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.hayden.test_graph",
        "com.hayden.commitdiffmodel",
        "com.hayden.proto",
        "com.hayden.utilitymodule.config"
})
public class CucumberTestConfig { }
