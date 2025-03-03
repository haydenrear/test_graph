package com.hayden.test_graph.commit_diff_context.config;

import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.db.DbDataSourceTrigger;
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
    public DataSource validationDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.app")
    public DataSource appDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public DataSource dataSource(@ResettableThread DockerInitCtx dockerInitCtx,
                                 DbDataSourceTrigger dbDataSourceTrigger) {
        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                String s = dockerInitCtx.getStarted()
                        .optional()
                        .filter(Boolean::booleanValue)
                        .map(b -> dbDataSourceTrigger.initializeKeyTo(DbDataSourceTrigger.APP_DB_KEY))
                        .orElse(DbDataSourceTrigger.VALIDATION_DB_KEY);
                return s;
            }
        };

        Map<Object, Object> resolvedDataSources = new HashMap<>();
        resolvedDataSources.put(DbDataSourceTrigger.APP_DB_KEY, appDataSource());
        resolvedDataSources.put(DbDataSourceTrigger.VALIDATION_DB_KEY, validationDataSource());

        routingDataSource.setTargetDataSources(resolvedDataSources);
        routingDataSource.setDefaultTargetDataSource(validationDataSource());


        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }

}
