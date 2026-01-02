package com.hayden.test_graph.init.selenium;

import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public final class SeleniumDriverFactory {

    private SeleniumDriverFactory() {}

    public static WebDriver create(
            Boolean recordVideo,
            String videoName,
            String videoScreenSize
    ) {
        boolean headless = resolveHeadless();
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        String chromeBinary = resolveChromeBinary();
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            options.setBinary(chromeBinary);
        }
        applyVideoOptions(options, recordVideo, videoName, videoScreenSize);
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        return new ChromeDriver(options);
    }

    private static boolean resolveHeadless() {
        String headlessEnv = System.getenv("SELENIUM_HEADLESS");
        if (headlessEnv != null && !headlessEnv.isBlank()) {
            return !"false".equalsIgnoreCase(headlessEnv);
        }
        String headlessProp = System.getProperty("selenium.headless", "true");
        return !"false".equalsIgnoreCase(headlessProp);
    }

    private static void applyVideoOptions(
            ChromeOptions options,
            Boolean recordVideo,
            String videoName,
            String videoScreenSize
    ) {
        if (!resolveRecordVideo(recordVideo)) {
            return;
        }
        options.setCapability("se:recordVideo", true);
        videoName = resolveVideoName(videoName);
        if (videoName != null && !videoName.isBlank()) {
            options.setCapability("se:videoName", videoName);
        }
        String screenSize = resolveVideoScreenSize(videoScreenSize);
        if (screenSize != null && !screenSize.isBlank()) {
            options.setCapability("se:screenSize", screenSize);
        }
        Map<String, Object> selenoidOptions = new HashMap<>();
        selenoidOptions.put("enableVideo", true);
        if (videoName != null && !videoName.isBlank()) {
            selenoidOptions.put("name", videoName);
        }
        if (screenSize != null && !screenSize.isBlank()) {
            selenoidOptions.put("screenResolution", screenSize);
        }
        options.setCapability("selenoid:options", selenoidOptions);
    }

    private static boolean resolveRecordVideo(Boolean recordVideo) {
        if (recordVideo != null) {
            return recordVideo;
        }
        String recordEnv = System.getenv("SELENIUM_RECORD_VIDEO");
        if (recordEnv != null && !recordEnv.isBlank()) {
            return "true".equalsIgnoreCase(recordEnv);
        }
        String recordProp = System.getProperty("selenium.recordVideo");
        return "true".equalsIgnoreCase(recordProp);
    }

    private static String resolveVideoName(String configured) {
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        String videoName = System.getenv("SELENIUM_VIDEO_NAME");
        if (videoName == null || videoName.isBlank()) {
            videoName = System.getProperty("selenium.video.name");
        }
        return videoName;
    }

    private static String resolveVideoScreenSize(String configured) {
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        String screenSize = System.getenv("SELENIUM_VIDEO_SCREEN_SIZE");
        if (screenSize == null || screenSize.isBlank()) {
            screenSize = System.getProperty("selenium.video.screenSize");
        }
        return screenSize;
    }

    private static String resolveChromeBinary() {
        String binary = System.getProperty("selenium.chrome.binary");
        if (binary == null || binary.isBlank()) {
            binary = System.getProperty("webdriver.chrome.bin");
        }
        if (binary == null || binary.isBlank()) {
            binary = System.getenv("SELENIUM_CHROME_BINARY");
        }
        if (binary == null || binary.isBlank()) {
            binary = System.getenv("CHROME_BINARY");
        }
        return binary;
    }
}
