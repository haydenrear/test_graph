package com.hayden.test_graph.multi_agent_ide.init.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepBubble;
import com.hayden.test_graph.multi_agent_ide.init.nodes.MultiAgentIdeInitNode;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Init context for multi-agent-ide test graph.
 * Configures docker-compose startup, event subscription types, and git repositories.
 */
@Component
@ResettableThread
@RequiredArgsConstructor
public class MultiAgentIdeInit implements InitCtx {

    @Builder
    public record DockerComposeConfig(
            Path composePath,
            String serviceNames,
            Integer startupTimeoutSeconds
    ) {
        public DockerComposeConfig(Path composePath) {
            this(composePath, "multi-agent-ide", 60);
        }
    }

    @Builder
    public record EventSubscriptionConfig(
            String subscriptionType,  // websocket, http_polling, kafka, etc.
            String brokerUrl,
            Integer reconnectDelayMs
    ) {
        public EventSubscriptionConfig(String subscriptionType) {
            this(subscriptionType, null, 5000);
        }
    }

    @Builder
    public record GitRepositoryConfig(
            Path repositoryPath,
            String initialBranch,
            boolean initializeWorktrees,
            boolean hasSubmodules,
            java.util.List<String> submoduleNames
    ) {
        public GitRepositoryConfig(Path repositoryPath) {
            this(repositoryPath, "main", true, false, java.util.List.of());
        }

        public GitRepositoryConfig(Path repositoryPath, java.util.List<String> submoduleNames) {
            this(repositoryPath, "main", true, !submoduleNames.isEmpty(), submoduleNames);
        }
    }

    @Builder
    public record SpecFileConfig(
            String specFileName,  // e.g., ".multi-agent-plan.md"
            String specFormat,    // "markdown" or "yaml"
            java.util.List<String> requiredSections  // ["Header", "Plan", "Status"]
    ) {
        public SpecFileConfig() {
            this(".multi-agent-plan.md", "markdown", java.util.List.of("Header", "Plan", "Status"));
        }
    }

    private final ContextValue<DockerComposeConfig> dockerComposeConfig = ContextValue.empty();
    private final ContextValue<EventSubscriptionConfig> eventSubscriptionConfig = ContextValue.empty();
    private final ContextValue<GitRepositoryConfig> gitRepositoryConfig = ContextValue.empty();
    private final ContextValue<SpecFileConfig> specFileConfig = ContextValue.empty();
    private final Map<String, Object> mockResponses = new HashMap<>();
    
    @Getter
    private final ContextValue<String> testListenerId = ContextValue.empty();

    MultiAgentIdeBubble bubble;

    @Autowired
    @ResettableThread
    public void setBubble(MultiAgentIdeBubble bubble) {
        this.bubble = bubble;
    }

    public void setDockerComposeConfig(DockerComposeConfig config) {
        dockerComposeConfig.set(config);
    }

    public DockerComposeConfig getDockerComposeConfig() {
        return dockerComposeConfig.get();
    }

    public void setEventSubscriptionConfig(EventSubscriptionConfig config) {
        eventSubscriptionConfig.set(config);
    }

    public EventSubscriptionConfig getEventSubscriptionConfig() {
        return eventSubscriptionConfig.get();
    }

    public void setGitRepositoryConfig(GitRepositoryConfig config) {
        gitRepositoryConfig.set(config);
    }

    public GitRepositoryConfig getGitRepositoryConfig() {
        return gitRepositoryConfig.get();
    }

    public void setSpecFileConfig(SpecFileConfig config) {
        specFileConfig.set(config);
    }

    public SpecFileConfig getSpecFileConfig() {
        return specFileConfig.get();
    }

    public void registerMockResponse(String key, Object response) {
        mockResponses.put(key, response);
    }

    public Object getMockResponse(String key) {
        return mockResponses.get(key);
    }

    @Override
    public List<Class<? extends TestGraphContext>> dependsOn() {
        return List.of();
    }

    @Override
    public Class<MultiAgentIdeBubble> bubbleClazz() {
        return MultiAgentIdeBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof MultiAgentIdeInitNode;
    }

    @Override
    public MultiAgentIdeBubble bubble() {
        return bubble;
    }
}
