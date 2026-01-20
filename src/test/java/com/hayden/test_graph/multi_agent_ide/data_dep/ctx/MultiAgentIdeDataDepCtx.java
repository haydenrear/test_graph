package com.hayden.test_graph.multi_agent_ide.data_dep.ctx;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.data_dep.ctx.DataDepCtx;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.selenium.ctx.SeleniumInitCtx;
import com.hayden.test_graph.multi_agent_ide.data_dep.nodes.MultiAgentIdeDataDepNode;
import com.hayden.test_graph.multi_agent_ide.init.ctx.MultiAgentIdeInit;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;

/**
 * Data dependency context for multi-agent-ide test graph.
 * Sets up test data, test event listeners, and prepares for execution.
 */
@Component
@ResettableThread
@RequiredArgsConstructor
public class MultiAgentIdeDataDepCtx implements DataDepCtx {

    private MultiAgentIdeDataDepBubble bubble;

    private Assertions assertions;

    @Builder
    public record TestEventListenerConfig(
            String listenerId,
            Integer maxEventBufferSize,
            Long eventTimeoutMs
    ) {
    }

    @Builder
    public record AgentConfig(
            boolean useStreamingModel,
            String modelType,
            Map<String, String> mockResponses
    ) {
        public AgentConfig() {
            this(true, "claude-haiku", new HashMap<>());
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
    }

    @Builder
    public record OrchestrationRequestConfig(
            String baseUrl,
            String goal,
            String repositoryUrl,
            String baseBranch,
            String title,
            String nodeId,
            Long waitTimeoutMs,
            Integer expectedEventCount
    ) {
    }

    @Builder
    public record UiEventObservation(
            String id,
            String type,
            String nodeId,
            Map<String, Object> rawEvent,
            Map<String, Object> payload
    ) {}

    /**
     * Thread-safe queue for storing events received from the subscription.
     * Events are stored as generic Object type and transferred to assert context.
     */
    public static class EventQueue {
        private final Queue<Object> events = new LinkedList<>();
        @Setter
        @Getter
        private volatile boolean subscriptionActive = false;

        @Setter
        Callable<Void> closeConnection;

        public void close() throws Exception {
            if (closeConnection != null)
                closeConnection.call();
        }

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

    }


    private final ContextValue<TestEventListenerConfig> eventListenerConfig = ContextValue.empty();
    private final ContextValue<AgentConfig> agentConfig = ContextValue.empty();
    private final ContextValue<SubmoduleConfig> submoduleConfig = ContextValue.empty();
    private final ContextValue<EventSubscriptionConfig> eventSubscriptionConfig = ContextValue.empty();
    private final ContextValue<SeleniumInitCtx.SeleniumData> seleniumUiConfig = ContextValue.empty();
    private final ContextValue<Integer> expectedEventCount = ContextValue.empty();
    private final ContextValue<MultiAgentIdeInit> initCtx = ContextValue.empty();
    @Getter
    private final EventQueue eventQueue = new EventQueue();
    @Getter
    private final List<OrchestrationRequestConfig> orchestrationRequests = new ArrayList<>();
    @Getter
    private final ContextValue<MultiAgentIdeInit> initContext = ContextValue.empty();

    public EventSubscriptionConfig getEventSubscriptionConfig() {
        return eventSubscriptionConfig.optional().orElse(null);
    }

    public void setEventSubscriptionConfig(EventSubscriptionConfig eventSubscriptionConfig) {
        this.eventSubscriptionConfig.set(eventSubscriptionConfig);
    }

    @Autowired
    @ResettableThread
    public void setBubble(MultiAgentIdeDataDepBubble bubble) {
        this.bubble = bubble;
    }

    @Autowired
    @ResettableThread
    public void setAssertions(Assertions assertions) {
        this.assertions = assertions;
    }

    public void setInitCtx(MultiAgentIdeInit init) {
        initCtx.set(init);
    }

    public MultiAgentIdeInit getInitCtx() {
        return initCtx.get();
    }

    public void setEventListenerConfig(TestEventListenerConfig config) {
        eventListenerConfig.set(config);
    }

    public TestEventListenerConfig getEventListenerConfig() {
        return eventListenerConfig.get();
    }

    public void setAgentConfig(AgentConfig config) {
        agentConfig.set(config);
    }

    public AgentConfig getAgentConfig() {
        return agentConfig.get();
    }

    public void setSubmoduleConfig(SubmoduleConfig config) {
        submoduleConfig.set(config);
    }

    public SubmoduleConfig getSubmoduleConfig() {
        return submoduleConfig.get();
    }

    public void setSeleniumUiConfig(SeleniumInitCtx.SeleniumData config) {
        seleniumUiConfig.set(config);
    }

    public SeleniumInitCtx.SeleniumData getSeleniumUiConfig() {
        return seleniumUiConfig.res().orElse(null);
    }

    public void setExpectedEventCount(Integer count) {
        expectedEventCount.set(count);
    }

    public Integer getExpectedEventCount() {
        return expectedEventCount.get();
    }

    public void addUiEvents(List<UiEventObservation> events) {
        for (var e : events)
            this.getEventQueue().enqueue(e);
    }

    public void registerClose(Callable<Void> toCall) {
        this.getEventQueue().setCloseConnection(toCall);
    }

    public void addOrchestrationRequest(OrchestrationRequestConfig config) {
        if (config == null) {
            return;
        }
        orchestrationRequests.add(config);
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

}
