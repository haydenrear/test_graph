package com.hayden.test_graph.init.selenium.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "selenium-init-ctx")
@Component
@Data
public class SeleniumInitConfigProps {

    boolean seleniumEnabled;

    String driver;

    String chromeDriverPath;

    String chromeBinaryPath;

    Boolean headless;

    Boolean recordVideo;

    String videoName;

    String videoOutputPath;

    String videoScreenSize;

    String baseUrl;

    Long waitTimeoutMs;

}
