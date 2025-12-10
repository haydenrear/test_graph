package com.hayden.test_graph.multi_agent_ide.data_dep.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.data_dep.ctx.DataDepCtx;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx.MultiAgentIdeAssertBubble;
import com.hayden.test_graph.multi_agent_ide.data_dep.nodes.MultiAgentIdeDataDepNode;
import com.hayden.test_graph.multi_agent_ide.init.ctx.MultiAgentIdeInit;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Data dependency context for multi-agent-ide test graph.
 * Sets up test data, test event listeners, and prepares for execution.
 */
@Component
@ResettableThread
@RequiredArgsConstructor
public class MultiAgentIdeDataDepCtx implements DataDepCtx {

    private MultiAgentIdeDataDepBubble bubble;

    @Builder
    public record TestEventListenerConfig(
            String listenerId,
            Integer maxEventBufferSize,
            Long eventTimeoutMs
    ) {
        public TestEventListenerConfig(String listenerId) {
            this(listenerId, 1000, 30000L);
        }
    }

    @Builder
    public record LangChain4jMockConfig(
            boolean useStreamingModel,
            String modelType,
            Map<String, String> mockResponses
    ) {
        public LangChain4jMockConfig() {
            this(true, "claude-haiku", new HashMap<>());
        }
    }

    @Builder
    public record SpecToolsConfig(
            boolean enableValidation,
            boolean enableGetSummary,
            boolean enableGetSection,
            boolean enableMerge,
            Integer maxContextTokens,
            boolean cacheSpecSummaries
    ) {
        public SpecToolsConfig() {
            this(true, true, true, true, 8000, true);
        }
    }

    @Builder
    public record SubmoduleConfig(
            boolean enableSubmoduleSupport,
            boolean autoDetectSubmodules,
            List<String> submoduleNames,
            Map<String, String> submodulePathMap  // name -> path mapping
    ) {
        public SubmoduleConfig() {
            this(true, true, new ArrayList<>(), new HashMap<>());
        }
    }

    @Builder
    public record EventSubscriptionConfig(
            String subscriptionProtocol,  // "websocket", "http", "kafka", etc.
            String eventEndpoint,
            Integer pollIntervalMs,
            Long subscriptionTimeoutMs,
            boolean autoStart
    ) {
        public EventSubscriptionConfig(String subscriptionProtocol, String eventEndpoint) {
            this(subscriptionProtocol, eventEndpoint, 100, 30000L, true);
        }
    }

    /**
     * Thread-safe queue for storing events received from the subscription.
     * Events are stored as generic Object type and transferred to assert context.
     */
    public static class EventQueue {
        private final Queue<Object> events = new LinkedList<>();
        private volatile boolean subscriptionActive = false;

        public void enqueue(Object event) {
            synchronized (events) {
                events.offer(event);
            }
        }

        public Object dequeue() {
            synchronized (events) {
                return events.poll();
            }
        }

        public Object peek() {
            synchronized (events) {
                return events.peek();
            }
        }

        public int size() {
            synchronized (events) {
                return events.size();
            }
        }

        public boolean isEmpty() {
            synchronized (events) {
                return events.isEmpty();
            }
        }

        public List<Object> drainAll() {
            synchronized (events) {
                List<Object> allEvents = new ArrayList<>(events);
                events.clear();
                return allEvents;
            }
        }

        public void setSubscriptionActive(boolean active) {
            this.subscriptionActive = active;
        }

        public boolean isSubscriptionActive() {
            return subscriptionActive;
        }
    }

    private final ContextValue<TestEventListenerConfig> eventListenerConfig = ContextValue.empty();
    private final ContextValue<LangChain4jMockConfig> langChain4jMockConfig = ContextValue.empty();
    private final ContextValue<SpecToolsConfig> specToolsConfig = ContextValue.empty();
    private final ContextValue<SubmoduleConfig> submoduleConfig = ContextValue.empty();
    private final ContextValue<EventSubscriptionConfig> eventSubscriptionConfig = ContextValue.empty();
    private final EventQueue eventQueue = new EventQueue();
    private final Map<String, Object> testData = new HashMap<>();

    @Getter
    private final ContextValue<MultiAgentIdeInit> initContext = ContextValue.empty();

    public void setEventSubscriptionConfig(EventSubscriptionConfig config) {
        eventSubscriptionConfig.set(config);
    }

    public EventSubscriptionConfig getEventSubscriptionConfig() {
        return eventSubscriptionConfig.get();
    }

    public EventQueue getEventQueue() {
        return eventQueue;
    }

    @Autowired
    @ResettableThread
    public void setBubble(MultiAgentIdeDataDepBubble bubble) {
        this.bubble = bubble;
    }

    public void setEventListenerConfig(TestEventListenerConfig config) {
        eventListenerConfig.set(config);
    }

    public TestEventListenerConfig getEventListenerConfig() {
        return eventListenerConfig.get();
    }

    public void setLangChain4jMockConfig(LangChain4jMockConfig config) {
        langChain4jMockConfig.set(config);
    }

    public LangChain4jMockConfig getLangChain4jMockConfig() {
        return langChain4jMockConfig.get();
    }

    public void setSpecToolsConfig(SpecToolsConfig config) {
        specToolsConfig.set(config);
    }

    public SpecToolsConfig getSpecToolsConfig() {
        return specToolsConfig.get();
    }

    public void setSubmoduleConfig(SubmoduleConfig config) {
        submoduleConfig.set(config);
    }

    public SubmoduleConfig getSubmoduleConfig() {
        return submoduleConfig.get();
    }

    public void putTestData(String key, Object value) {
        testData.put(key, value);
    }

    public Object getTestData(String key) {
        return testData.get(key);
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof MultiAgentIdeDataDepNode;
    }

    @Override
    public DataDepBubble bubble() {
        return bubble;
    }

    @Override
    public Class<MultiAgentIdeDataDepBubble> bubbleClazz() {
        return MultiAgentIdeDataDepBubble.class;
    }

    @Override
    public List<Class<? extends TestGraphContext>> dependsOn() {
        return List.of();
    }
}
