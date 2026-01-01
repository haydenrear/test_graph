package com.hayden.test_graph.multi_agent_ide.util;

import java.net.MalformedURLException;
import java.net.URL;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public final class SeleniumDriverFactory {

    private SeleniumDriverFactory() {}

    public static WebDriver create(String driverName) {
        String remoteUrl = System.getenv("SELENIUM_REMOTE_URL");
        if (remoteUrl != null && !remoteUrl.isBlank()) {
            return buildRemoteDriver(remoteUrl);
        }

        String resolvedDriver = driverName;
        if (resolvedDriver == null || resolvedDriver.isBlank()) {
            resolvedDriver = System.getProperty("selenium.driver", "chrome");
        }

        boolean headless = resolveHeadless();
        if ("firefox".equalsIgnoreCase(resolvedDriver)) {
            FirefoxOptions options = new FirefoxOptions();
            if (headless) {
                options.addArguments("-headless");
            }
            return new FirefoxDriver(options);
        }

        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        return new ChromeDriver(options);
    }

    private static WebDriver buildRemoteDriver(String remoteUrl) {
        try {
            ChromeOptions options = new ChromeOptions();
            if (resolveHeadless()) {
                options.addArguments("--headless=new");
            }
            return new RemoteWebDriver(new URL(remoteUrl), options);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid SELENIUM_REMOTE_URL: " + remoteUrl, e);
        }
    }

    private static boolean resolveHeadless() {
        String headlessEnv = System.getenv("SELENIUM_HEADLESS");
        if (headlessEnv != null && !headlessEnv.isBlank()) {
            return !"false".equalsIgnoreCase(headlessEnv);
        }
        String headlessProp = System.getProperty("selenium.headless", "true");
        return !"false".equalsIgnoreCase(headlessProp);
    }
}
