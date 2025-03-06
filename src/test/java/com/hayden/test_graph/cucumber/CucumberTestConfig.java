package com.hayden.test_graph.cucumber;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.hayden.test_graph",
        "com.hayden.commitdiffmodel",
        "com.hayden.proto",
        "com.hayden.utilitymodule.config",
        "com.hayden.utilitymodule.db"
})
public class CucumberTestConfig { }
