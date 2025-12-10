package com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.assert_g.ctx.AssertCtx;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.ctx.CommitDiffContextIndexingAssertBubble;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.nodes.MultiAgentIdeAssertNode;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepCtx;
import com.hayden.test_graph.multi_agent_ide.init.ctx.MultiAgentIdeInit;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Assert context for multi-agent-ide test graph.
 * Validates execution results and manages test assertions.
 */
@Component
@ResettableThread
@RequiredArgsConstructor
public class MultiAgentIdeAssertCtx implements AssertCtx {

    private MultiAgentIdeAssertBubble bubble;

    @Builder
    public record EventAssertions(
            int expectedEventCount,
            List<String> eventTypesExpected,
            boolean validateEventOrder
    ) {
        public EventAssertions() {
            this(0, new ArrayList<>(), true);
        }
    }

    @Builder
    public record GraphAssertions(
            int expectedNodeCount,
            int expectedWorktreeCount,
            List<String> expectedNodeStatuses
    ) {
        public GraphAssertions() {
            this(0, 0, new ArrayList<>());
        }
    }

    private Assertions assertions;

    @Autowired
    public void setAssertions(Assertions assertions) {
        this.assertions = assertions;
    }

    @Getter
    private final ContextValue<MultiAgentIdeInit> initContext = ContextValue.empty();

    @Getter
    private final ContextValue<MultiAgentIdeDataDepCtx> dataDepContext = ContextValue.empty();

    private final ContextValue<EventAssertions> eventAssertions = ContextValue.empty();
    private final ContextValue<GraphAssertions> graphAssertions = ContextValue.empty();
    private final List<Object> capturedEvents = new ArrayList<>();
    private final Map<String, Object> assertionResults = new HashMap<>();
    private MultiAgentIdeDataDepCtx.EventQueue eventQueueFromDataDep;

    @Autowired
    @ResettableThread
    public void setBubble(MultiAgentIdeAssertBubble bubble) {
        this.bubble = bubble;
    }

    public void setInitContext(MultiAgentIdeInit init) {
        initContext.swap(init);
    }

    public void setDataDepContext(MultiAgentIdeDataDepCtx dataDep) {
        dataDepContext.swap(dataDep);
    }

    public void setEventAssertions(EventAssertions assertions) {
        eventAssertions.swap(assertions);
    }

    public EventAssertions getEventAssertions() {
        return eventAssertions.get();
    }

    public void setGraphAssertions(GraphAssertions assertions) {
        graphAssertions.swap(assertions);
    }

    public GraphAssertions getGraphAssertions() {
        return graphAssertions.get();
    }

    public void addCapturedEvent(Object event) {
        capturedEvents.add(event);
    }

    public List<Object> getCapturedEvents() {
        return capturedEvents;
    }

    public void putAssertionResult(String key, Object result) {
        assertionResults.put(key, result);
    }

    public Object getAssertionResult(String key) {
        return assertionResults.get(key);
    }

    /**
     * Transfer the event queue from data dep context to assert context.
     * This should be called after data dep phase to make events available for assertions.
     */
    public void transferEventQueueFromDataDep(MultiAgentIdeDataDepCtx dataDepCtx) {
        if (dataDepCtx != null) {
            this.eventQueueFromDataDep = dataDepCtx.getEventQueue();
        }
    }

    /**
     * Get the event queue that was transferred from data dep context.
     * Contains all events received during the polling phase.
     */
    public MultiAgentIdeDataDepCtx.EventQueue getEventQueueFromDataDep() {
        return eventQueueFromDataDep;
    }

    /**
     * Get all events from the transferred queue as a list.
     * This drains the queue, so subsequent calls will return an empty list.
     */
    public List<Object> getAllEventsFromQueue() {
        if (eventQueueFromDataDep != null) {
            return eventQueueFromDataDep.drainAll();
        }
        return new ArrayList<>();
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof MultiAgentIdeAssertNode;
    }

    @Override
    public MultiAgentIdeAssertBubble bubble() {
        return bubble;
    }

    @Override
    public Class<MultiAgentIdeAssertBubble> bubbleClazz() {
        return MultiAgentIdeAssertBubble.class;
    }

    @Override
    public List<Class<? extends TestGraphContext>> dependsOn() {
        return List.of();
    }
}
