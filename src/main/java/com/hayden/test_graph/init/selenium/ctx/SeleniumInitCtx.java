package com.hayden.test_graph.init.selenium.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.docker.config.DockerInitConfigProps;
import com.hayden.test_graph.init.selenium.config.SeleniumInitConfigProps;
import com.hayden.test_graph.init.selenium.exec.SeleniumInitNode;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ResettableThread
public class SeleniumInitCtx implements InitCtx {

    @Builder
    public record SeleniumUiConfig(
            String baseUrl,
            Long waitTimeoutMs,
            String driver,
            Boolean recordVideo,
            String videoName,
            String videoOutputPath,
            String videoScreenSize
    ) { }

    public record SeleniumData(WebDriver driver, SeleniumInitCtx.SeleniumUiConfig config) {}

    private SeleniumInitBubbleCtx seleniumInitBubbleCtx;

    @Getter
    private ContextValue<SeleniumData> driver = ContextValue.empty();


    private final ContextValue<SeleniumUiConfig> context = ContextValue.empty();


    public void setDriver(WebDriver data) {
        driver.set(new SeleniumData(data, context.optional().orElse(null)));
    }

    public Optional<SeleniumUiConfig> getConfig() {
        return context.optional();
    }

    public void setConfig(SeleniumUiConfig config) {
        context.set(config);
    }

    @Autowired
    public void setDockerInitBubbleCtx(SeleniumInitBubbleCtx seleniumInitBubbleCtx) {
        this.seleniumInitBubbleCtx = seleniumInitBubbleCtx;
    }

    @Override
    public SeleniumInitBubbleCtx bubble() {
        return seleniumInitBubbleCtx;
    }

    @Override
    public Class<SeleniumInitBubbleCtx> bubbleClazz() {
        return SeleniumInitBubbleCtx.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof SeleniumInitNode;
    }

}
