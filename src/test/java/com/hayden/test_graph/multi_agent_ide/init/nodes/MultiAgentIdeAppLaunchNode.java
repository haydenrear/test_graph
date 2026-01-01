package com.hayden.test_graph.multi_agent_ide.init.nodes;

import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.multi_agent_ide.init.ctx.MultiAgentIdeInit;
import com.hayden.test_graph.thread.ResettableThread;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ResettableThread
public class MultiAgentIdeAppLaunchNode implements MultiAgentIdeInitNode {

    @Override
    public MultiAgentIdeInit exec(MultiAgentIdeInit ctx, MetaCtx h) {
        MultiAgentIdeInit.AppLaunchConfig config = ctx.getAppLaunchConfig();
        if (config == null) {
            return ctx;
        }

        int port = config.port() != null ? config.port() : 8080;
        String baseUrl = config.baseUrl() != null && !config.baseUrl().isBlank()
                ? config.baseUrl()
                : "http://localhost:" + port;

        if (config.skipIfHealthy() && isHealthy(baseUrl)) {
            log.info("Multi-agent IDE already responding at {}", baseUrl);
            return ctx;
        }

        String jarPath = resolveJarPath(config.jarPath());
        List<String> command = new ArrayList<>();
        command.add("java");
        if (config.jvmArgs() != null) {
            command.addAll(config.jvmArgs());
        }
        command.add("-jar");
        command.add(jarPath);
        command.add("--server.port=" + port);
        if (config.worktreesBasePath() != null && !config.worktreesBasePath().isBlank()) {
            command.add("--multiagentide.worktrees.base-path=" + config.worktreesBasePath());
        }
        if (config.appArgs() != null) {
            command.addAll(config.appArgs());
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            ctx.setAppProcess(process);
            waitForHealthy(baseUrl, 30000L);
            log.info("Started multi-agent IDE from {} on {}", jarPath, baseUrl);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start multi-agent IDE", e);
        }

        return ctx;
    }

    private boolean isHealthy(String baseUrl) {
        try {
            URI uri = URI.create(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            return code >= 200 && code < 500;
        } catch (IOException e) {
            return false;
        }
    }

    private void waitForHealthy(String baseUrl, long timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (isHealthy(baseUrl)) {
                return;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new IllegalStateException("Timed out waiting for multi-agent IDE at " + baseUrl);
    }

    private String resolveJarPath(String configuredPath) {
        if (configuredPath != null && !configuredPath.isBlank()) {
            return configuredPath;
        }
        String envPath = System.getenv("MULTI_AGENT_IDE_JAR");
        if (envPath != null && !envPath.isBlank()) {
            return envPath;
        }
        String propPath = System.getProperty("multiagentide.jar");
        if (propPath != null && !propPath.isBlank()) {
            return propPath;
        }

        Path repoRoot = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        if (repoRoot.getFileName() != null && "test_graph".equals(repoRoot.getFileName().toString())) {
            repoRoot = repoRoot.getParent();
        }
        Path libsDir = repoRoot.resolve("multi_agent_ide").resolve("build").resolve("libs");
        if (!Files.exists(libsDir)) {
            throw new IllegalStateException("Jar not found; build multi_agent_ide or set MULTI_AGENT_IDE_JAR");
        }

        try (Stream<Path> jars = Files.list(libsDir)) {
            return jars.filter(path -> path.toString().endsWith(".jar"))
                    .filter(path -> !path.getFileName().toString().endsWith("-plain.jar"))
                    .max(Comparator.comparingLong(path -> path.toFile().lastModified()))
                    .map(Path::toString)
                    .orElseThrow(() -> new IllegalStateException("No runnable jar found in " + libsDir));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to resolve jar path", e);
        }
    }
}
