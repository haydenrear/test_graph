package com.hayden.test_graph.multi_agent_ide.data_dep.nodes;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.init.selenium.ctx.SeleniumInitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepCtx;
import com.hayden.test_graph.thread.ResettableThread;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.hayden.test_graph.multi_agent_ide.MultiAgentTestTimeout.REQUEST_TIMEOUT;

@Component
@ResettableThread
@Slf4j
public class SeleniumUiDataDepNode implements MultiAgentIdeDataDepNode {

    @Autowired
    SeleniumInitCtx seleniumInitCtx;
    @Autowired
    Assertions assertions;

    AtomicBoolean keepGoing = new AtomicBoolean(true);

    @Override
    public MultiAgentIdeDataDepCtx exec(MultiAgentIdeDataDepCtx ctx, MetaCtx h) {
        var configOpt = seleniumInitCtx.getDriver();


        if (configOpt.isEmpty()) {
            return ctx;
        }

        ctx.setSeleniumUiConfig(configOpt.get());

        var config = configOpt.get().config();

        String baseUrl = resolveBaseUrl(config.baseUrl());
        long waitTimeout = config.waitTimeoutMs() != null ? config.waitTimeoutMs() : REQUEST_TIMEOUT * 1000L;

        var driver = configOpt.get().driver();

        try {
            driver.get(baseUrl);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(waitTimeout));

            wait.pollingEvery(Duration.ofSeconds(3))
                    .withTimeout(Duration.ofMillis(waitTimeout))
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='goal-input']")));

            ctx.registerClose(() -> {
                log.info("Closing selenium.");
                this.keepGoing.set(false);
                driver.quit();
                return null;
            });

            CompletableFuture.runAsync(() -> {
                        JavascriptExecutor executor = (JavascriptExecutor) driver;
                        executor.executeScript("window.__agUiTestProbe = window.__agUiTestProbe || { events: [] };");
                        WebDriverWait waitAgain = new WebDriverWait(driver, Duration.ofSeconds(300));
                        waitAgain.pollingEvery(Duration.ofSeconds(3))
                                .withTimeout(Duration.ofSeconds(300))
                                .until(webDriver -> {
                                    ctx.addUiEvents(readUiEvents(executor));
                                    return !this.keepGoing.get();
                                });
                    })
                    .exceptionally(t -> {
                        assertions.assertSoftly(false, "Failed - %s".formatted(t.toString()));
                        return null;
                    });

        } catch (Exception e) {
            try {
                driver.quit();
            } catch (Exception ignored) {
                // ignore quit failures after error
            }
            throw new IllegalStateException("Failed to capture UI events via Selenium", e);
        }

        return ctx;
    }

    private String resolveBaseUrl(String configured) {
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        return "http://localhost:8080";
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

        if (!observations.isEmpty()) {
            log.info("Read {} UI events", observations.size());
        }

        return observations;
    }

    private String toString(Object value) {
        return value != null ? value.toString() : null;
    }

}
