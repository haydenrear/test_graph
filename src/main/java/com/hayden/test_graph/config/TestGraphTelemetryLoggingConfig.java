package com.hayden.test_graph.config;

import com.hayden.utilitymodule.telemetry.log.LoggingConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

//@Import(LoggingConfig.class)
@Configuration
@Profile("telemetry-logging")
public class TestGraphTelemetryLoggingConfig { }
