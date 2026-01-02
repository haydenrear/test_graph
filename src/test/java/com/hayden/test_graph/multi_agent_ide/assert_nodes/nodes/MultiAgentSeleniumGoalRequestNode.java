package com.hayden.test_graph.multi_agent_ide.assert_nodes.nodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.init.selenium.ctx.SeleniumInitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx.MultiAgentIdeAssertCtx;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepCtx;
import com.hayden.test_graph.multi_agent_ide.edges.InitToAssertEdge;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ResettableThread
public class MultiAgentSeleniumGoalRequestNode implements MultiAgentIdeAssertNode {

    @Autowired
    private Assertions assertions;
    @Autowired
    private MultiAgentIdeDataDepCtx ctx;

    @Override
    public MultiAgentIdeAssertCtx exec(MultiAgentIdeAssertCtx c, MetaCtx h) {
        assertions.assertSoftly(CollectionUtils.isNotEmpty(ctx.getOrchestrationRequests()),
                "Did not contain any orchestration requests to run.");

        SeleniumInitCtx.SeleniumData seleniumUiConfig = ctx.getSeleniumUiConfig();

        if (seleniumUiConfig == null)
            return c;

        var driver = seleniumUiConfig.driver();

        for (MultiAgentIdeDataDepCtx.OrchestrationRequestConfig request : ctx.getOrchestrationRequests()) {
            var baseUrl = request.baseUrl();
            var waitTimeout = request.waitTimeoutMs();
            var goal = request.goal();
            var repoUrl = request.repositoryUrl();
            var baseBranch = request.baseBranch();
            driver.get(baseUrl);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(waitTimeout));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='goal-input']")));

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

        }

        return c;
    }

    @Override
    public List<Class<? extends MultiAgentIdeAssertNode>> dependsOn() {
        return List.of(InitToAssertEdge.class);
    }


}
