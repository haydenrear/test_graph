package com.hayden.test_graph.config;

import io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@ImportAutoConfiguration(
        exclude = {OpenTelemetryAutoConfiguration.class, org.springframework.boot.actuate.autoconfigure.tracing.OpenTelemetryAutoConfiguration.class})
@Profile("!telemetry-logging")
@Configuration
public class TestGraphDisableLoggingConfig { }
