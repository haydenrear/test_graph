package com.hayden.test_graph.init.selenium.exec;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.init.selenium.SeleniumDriverFactory;
import com.hayden.test_graph.init.selenium.config.SeleniumInitConfigProps;
import com.hayden.test_graph.init.selenium.ctx.SeleniumInitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@RequiredArgsConstructor
@Component
@ResettableThread
@Slf4j
public class StartSeleniumNode implements SeleniumInitNode {

    private final SeleniumInitConfigProps seleniumInitConfigProps;
    private final Assertions assertions;

    @Override
    public boolean skip(SeleniumInitCtx initCtx) {
        return !seleniumInitConfigProps.isSeleniumEnabled() && initCtx.getConfig().isEmpty();
    }

    @Override
    public SeleniumInitCtx exec(SeleniumInitCtx c, MetaCtx h) {
        var configOpt = c.getConfig();
        if (configOpt.isEmpty()) {
            return c;
        }

        var config = configOpt.get();

        applyDriverSettings();

        Boolean recordVideo = resolveValue(config.recordVideo(), seleniumInitConfigProps.getRecordVideo());
        String videoOutputPath = resolveValue(config.videoOutputPath(), seleniumInitConfigProps.getVideoOutputPath());
        String videoName = resolveVideoName(
                resolveValue(config.videoName(), seleniumInitConfigProps.getVideoName()),
                videoOutputPath
        );
        String videoScreenSize = resolveValue(config.videoScreenSize(), seleniumInitConfigProps.getVideoScreenSize());
        if (Boolean.TRUE.equals(recordVideo) && videoOutputPath != null && !videoOutputPath.isBlank()) {
            log.info("Recording selenium video to {}", videoOutputPath);
        }
        WebDriver driver = SeleniumDriverFactory.create(
                recordVideo,
                videoName,
                videoScreenSize
        );

        c.setDriver(driver);
        return c;
    }

    @Override
    public Class<SeleniumInitCtx> clzz() {
        return SeleniumInitCtx.class;
    }

    private String resolveVideoName(String configuredName, String outputPath) {
        if (configuredName != null && !configuredName.isBlank()) {
            return configuredName;
        }
        if (outputPath == null || outputPath.isBlank()) {
            return null;
        }
        Path path = Paths.get(outputPath);
        Path filename = path.getFileName();
        return filename != null ? filename.toString() : null;
    }

    private SeleniumInitCtx.SeleniumUiConfig configFromProperties() {
        if (!seleniumInitConfigProps.isSeleniumEnabled()) {
            return null;
        }
        return SeleniumInitCtx.SeleniumUiConfig.builder()
                .baseUrl(seleniumInitConfigProps.getBaseUrl())
                .waitTimeoutMs(seleniumInitConfigProps.getWaitTimeoutMs())
                .driver(seleniumInitConfigProps.getDriver())
                .recordVideo(seleniumInitConfigProps.getRecordVideo())
                .videoName(seleniumInitConfigProps.getVideoName())
                .videoOutputPath(seleniumInitConfigProps.getVideoOutputPath())
                .videoScreenSize(seleniumInitConfigProps.getVideoScreenSize())
                .build();
    }

    private void applyDriverSettings() {
        String driverPath = seleniumInitConfigProps.getChromeDriverPath();
        if (driverPath != null && !driverPath.isBlank()) {
            System.setProperty("webdriver.chrome.driver", driverPath);
        }
        String chromeBinary = seleniumInitConfigProps.getChromeBinaryPath();
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            System.setProperty("selenium.chrome.binary", chromeBinary);
        }
        Boolean headless = seleniumInitConfigProps.getHeadless();
        if (headless != null) {
            System.setProperty("selenium.headless", headless.toString());
        }
    }

    private static <T> T resolveValue(T configured, T fallback) {
        return configured != null ? configured : fallback;
    }
}
