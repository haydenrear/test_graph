package com.hayden.test_graph.config;

import com.hayden.test_graph.thread.ResettableThreadScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ScopeConfig {

    public static final String THREAD_SCOPE = "resettable_thread";

    @Bean
    public CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope(THREAD_SCOPE, resettableThreadScope());
        return configurer;
    }


    @Bean
    public ResettableThreadScope resettableThreadScope() {
        return new ResettableThreadScope();
    }

}
