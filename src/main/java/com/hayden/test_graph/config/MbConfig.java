package com.hayden.test_graph.config;

import org.mbtest.javabank.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MbConfig {

    @Bean
    public Client client() {
        return new Client("http://localhost:2525");
    }

}
