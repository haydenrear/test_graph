package com.hayden.test_graph.multi_agent_ide.init.nodes;

import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.multi_agent_ide.init.ctx.MultiAgentIdeInit;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.config.EnvConfigProps;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.hayden.test_graph.multi_agent_ide.MultiAgentTestTimeout.HEALTH_TIMEOUT;

@Slf4j
@Component
@ResettableThread
public class MultiAgentIdeAppLaunchNode implements MultiAgentIdeInitNode {

    @Autowired
    EnvConfigProps envConfigProps;

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

        runKillScript(port);

        log.info("Starting multi agent jar...");

        String jarPath = resolveJarPath(config.jarPath());
        List<String> command = new ArrayList<>();
        command.add("java");
        if (config.jvmArgs() != null) {
            command.addAll(config.jvmArgs());
        }
        command.add("-jar");
        command.add(jarPath);
        command.add("--server.port=" + port);
        if (config.profiles() != null && !config.profiles().isBlank()) {
            command.add("--spring.profiles.active=" + config.profiles());
        }
        if (config.worktreesBasePath() != null && !config.worktreesBasePath().isBlank()) {
            command.add("--multiagentide.worktrees.base-path=" + config.worktreesBasePath());
        }
        if (config.appArgs() != null) {
            command.addAll(config.appArgs());
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.environment().put("SPRING_PROFILES_ACTIVE", config.profiles());
            builder.environment().put("OPENAI_BASE_URL", "http://localhost:4545/v1");
            builder.redirectErrorStream(true);
            Process process = builder.start();
            CompletableFuture.runAsync(() -> {
                try(
                        var i = new InputStreamReader(process.getInputStream());
                        var br = new BufferedReader(i)
                ) {
                    br.lines().forEach(log::info);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            ctx.setAppProcess(process);
            waitForHealthy(baseUrl, HEALTH_TIMEOUT * 1000);
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
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new IllegalStateException("Timed out waiting for multi-agent IDE at " + baseUrl);
    }

    private void runKillScript(int port) {
        Path repoRoot = resolveProjectDir();
        Path scriptPath = repoRoot.resolve("test_graph").resolve("kill-java-jar.sh");
        if (!Files.exists(scriptPath)) {
            log.warn("kill-java-jar.sh not found at {}, skipping.", scriptPath);
            return;
        }
        List<String> command = List.of("zsh", scriptPath.toString(), String.valueOf(port));
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            String output = new String(process.getInputStream().readAllBytes());
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                log.warn("Timed out waiting for kill-java-jar.sh to complete.");
                return;
            }
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.warn("kill-java-jar.sh exited with status {}", exitCode);
            }
            if (!output.isBlank()) {
                Thread.sleep(2000);
                log.info("kill-java-jar.sh output:\n{}", output.trim());
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Failed to run kill-java-jar.sh", e);
        }
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

        Path repoRoot = resolveProjectDir();
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

    private Path resolveProjectDir() {
        Path configured = envConfigProps != null ? envConfigProps.getProjectDir().getParent() : null;
        if (configured != null) {
            return configured;
        }
        Path repoRoot = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        if (repoRoot.getFileName() != null && "test_graph".equals(repoRoot.getFileName().toString())) {
            repoRoot = repoRoot.getParent();
        }
        return repoRoot;
    }
}
