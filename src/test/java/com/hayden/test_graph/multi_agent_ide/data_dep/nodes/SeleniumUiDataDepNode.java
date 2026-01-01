package com.hayden.test_graph.multi_agent_ide.data_dep.nodes;

import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepCtx;
import com.hayden.test_graph.multi_agent_ide.util.SeleniumDriverFactory;
import com.hayden.test_graph.thread.ResettableThread;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ResettableThread
public class SeleniumUiDataDepNode implements MultiAgentIdeDataDepNode {

    @Override
    public MultiAgentIdeDataDepCtx exec(MultiAgentIdeDataDepCtx ctx, MetaCtx h) {
        MultiAgentIdeDataDepCtx.SeleniumUiConfig config = ctx.getSeleniumUiConfig();
        if (config == null) {
            return ctx;
        }

        String baseUrl = resolveBaseUrl(config.baseUrl());
        String goal = resolveGoal(config.goal());
        String repoUrl = resolveRepositoryUrl(config.repositoryUrl());
        String baseBranch = config.baseBranch() != null && !config.baseBranch().isBlank()
                ? config.baseBranch()
                : "main";
        long waitTimeout = config.waitTimeoutMs() != null ? config.waitTimeoutMs() : 30000L;
        Integer expectedCount = config.expectedEventCount() != null
                ? config.expectedEventCount()
                : ctx.getExpectedEventCount();

        String videoName = resolveVideoName(config.videoName(), config.videoOutputPath());
        if (Boolean.TRUE.equals(config.recordVideo()) && config.videoOutputPath() != null
                && !config.videoOutputPath().isBlank()) {
            log.info("Recording selenium video to {}", config.videoOutputPath());
        }
        WebDriver driver = SeleniumDriverFactory.create(
                config.recordVideo(),
                videoName,
                config.videoScreenSize()
        );
        try {
            driver.get(baseUrl);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(waitTimeout));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='goal-input']")));

            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("window.__agUiTestProbe = window.__agUiTestProbe || { events: [] };");

            WebElement goalInput = driver.findElement(By.cssSelector("[data-testid='goal-input']"));
            WebElement repoInput = driver.findElement(By.cssSelector("[data-testid='repo-input']"));
            WebElement branchInput = driver.findElement(By.cssSelector("[data-testid='branch-input']"));
            WebElement submit = driver.findElement(By.cssSelector("[data-testid='goal-submit']"));

            goalInput.clear();
            goalInput.sendKeys(goal);
            repoInput.clear();
            repoInput.sendKeys(repoUrl);
            branchInput.clear();
            branchInput.sendKeys(baseBranch);
            submit.click();

            wait.until(webDriver -> {
                Object count = ((JavascriptExecutor) webDriver)
                        .executeScript("return window.__agUiTestProbe.events.length;");
                long size = count instanceof Number ? ((Number) count).longValue() : 0L;
                return expectedCount != null ? size >= expectedCount : size > 0;
            });

            ctx.addUiEvents(readUiEvents(executor));
            ctx.registerClose(() -> {
                log.info("Closing selenium.");
                driver.close();
                return null;
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to capture UI events via Selenium", e);
        } finally {
            driver.quit();
        }

        return ctx;
    }

    private String resolveBaseUrl(String configured) {
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        return "http://localhost:8080";
    }

    private String resolveGoal(String configured) {
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        return "Validate UI event stream";
    }

    private String resolveRepositoryUrl(String configured) {
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        String repoRoot = System.getProperty("user.dir");
        if (repoRoot != null && repoRoot.endsWith("test_graph")) {
            return java.nio.file.Paths.get(repoRoot).getParent().toString();
        }
        return repoRoot;
    }

    private List<MultiAgentIdeDataDepCtx.UiEventObservation> readUiEvents(JavascriptExecutor executor) {
        Object events = executor.executeScript("return window.__agUiTestProbe.events;");
        if (!(events instanceof List<?> rawList)) {
            return List.of();
        }
        List<MultiAgentIdeDataDepCtx.UiEventObservation> observations = new ArrayList<>();
        for (Object entry : rawList) {
            if (!(entry instanceof Map<?, ?> rawMap)) {
                continue;
            }
            String id = toString(rawMap.get("id"));
            String type = toString(rawMap.get("type"));
            String nodeId = toString(rawMap.get("nodeId"));
            @SuppressWarnings("unchecked")
            Map<String, Object> rawEvent = rawMap.get("rawEvent") instanceof Map<?, ?>
                    ? (Map<String, Object>) rawMap.get("rawEvent")
                    : null;
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = rawMap.get("payload") instanceof Map<?, ?>
                    ? (Map<String, Object>) rawMap.get("payload")
                    : null;
            observations.add(MultiAgentIdeDataDepCtx.UiEventObservation.builder()
                    .id(id)
                    .type(type)
                    .nodeId(nodeId)
                    .rawEvent(rawEvent)
                    .payload(payload)
                    .build());
        }
        return observations;
    }

    private String toString(Object value) {
        return value != null ? value.toString() : null;
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
}
