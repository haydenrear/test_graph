package com.hayden.test_graph.commit_diff_context.config;

import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DbConfig {


    @Bean
    @ConfigurationProperties("spring.datasource.init")
    public DataSource initializationDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.initialized")
    public DataSource initializedDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public DataSource dataSource(@ResettableThread DockerInitCtx dockerInitCtx) {
        var d = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return dockerInitCtx.getStarted()
                        .optional().filter(Boolean::booleanValue)
                        .map(b -> "initialized")
                        .orElse("init");
            }
        };

        Map<Object, Object> resolvedDataSources = new HashMap<>();
        resolvedDataSources.put("initialized", initializedDataSource());
        resolvedDataSources.put("init", initializationDataSource());
        d.setTargetDataSources(resolvedDataSources);

        d.setDefaultTargetDataSource(initializationDataSource());

        d.afterPropertiesSet();
        return d;
    }

}
