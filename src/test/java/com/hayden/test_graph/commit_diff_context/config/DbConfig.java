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
    @ConfigurationProperties("spring.datasource.validation")
    public DataSource initializationDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.app")
    public DataSource initializedDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public DataSource dataSource(@ResettableThread DockerInitCtx dockerInitCtx,
                                 DbDataSourceTrigger dbDataSourceTrigger) {
        var d = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return dockerInitCtx.getStarted()
                        .optional()
                        .filter(Boolean::booleanValue)
                        .map(b -> dbDataSourceTrigger.initializeGetKey())
                        .orElse(DbDataSourceTrigger.VALIDATION_DB_KEY);
            }
        };

        Map<Object, Object> resolvedDataSources = new HashMap<>();
        resolvedDataSources.put(DbDataSourceTrigger.APP_DB_KEY, initializedDataSource());
        resolvedDataSources.put(DbDataSourceTrigger.VALIDATION_DB_KEY, initializationDataSource());
        d.setTargetDataSources(resolvedDataSources);

        d.setDefaultTargetDataSource(initializationDataSource());

        d.afterPropertiesSet();
        return d;
    }

}
