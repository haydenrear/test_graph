package com.hayden.test_graph.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class SpringBeansConfig {

    @Bean
    public PathMatchingResourcePatternResolver resolver() {
        return new PathMatchingResourcePatternResolver();
    }

}
