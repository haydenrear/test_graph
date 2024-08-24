package com.hayden.test_graph.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.SimpleThreadScope;

@Slf4j
@Configuration
public class ScopeConfig {

    public static final String THREAD_SCOPE = "thread";

    @Bean
    public CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope(THREAD_SCOPE, simpleThreadScope());
        return configurer;
    }


    SimpleThreadScope simpleThreadScope() {
        return new SimpleThreadScope();
    }

}
