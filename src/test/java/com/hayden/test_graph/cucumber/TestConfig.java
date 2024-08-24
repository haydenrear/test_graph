package com.hayden.test_graph.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@CucumberContextConfiguration
@SpringBootTest(classes = CucumberTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAspectJAutoProxy
public class TestConfig {
}
