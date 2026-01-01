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

    // ============ SEALED ASSERTION TYPE HIERARCHY ============
    // Extensible sealed interface pattern for type-safe assertion handling.
    // Add new assertion types by creating new records that implement this interface.

    /**
     * Sealed interface for all MultiAgentIde assertion types.
     * Enables pattern matching and exhaustive checking of assertion types.
     * Extend this interface to add new assertion types for other feature files.
     */
    public sealed interface MultiAgentIdeAssertion permits
            EventAssertion,
            NodeStatusAssertion,
            NodePersistenceAssertion,
            NodeCapabilityAssertion,
            ChildNodeCountAssertion,
            CompletionStatusAssertion,
            EventCountAssertion,
            UniqueNodeIdAssertion,
            DatabasePersistenceAssertion,
            ProtocolReceptionAssertion,
            EventDeserializationAssertion,
            NodeIdMatchAssertion,
            MessageBusPublicationAssertion,
            EventContentAssertion,
            NodeStatusChangedEventAssertion
    {}

    /**
     * Assertion for event reception and validation.
     */
    @Builder
    public record EventAssertion(
            String eventType,
            String nodeType,
            String nodeId,
            String payloadFile,
            boolean shouldExist
    ) implements MultiAgentIdeAssertion {}

    /**
     * Assertion for node status validation.
     */
    @Builder
    public record NodeStatusAssertion(
            String nodeId,
            String expectedStatus,
            String assertionType
    ) implements MultiAgentIdeAssertion {}

    /**
     * Assertion for node persistence in database.
     */
    @Builder
    public record NodePersistenceAssertion(
            String nodeId,
            String assertionType,
            boolean shouldBePersisted,
            String database
    ) implements MultiAgentIdeAssertion {}

    /**
     * Assertion for node capabilities (Branchable, Interruptable, etc).
     */
    @Builder
    public record NodeCapabilityAssertion(
            String nodeId,
            String capability,
            boolean shouldHaveCapability
    ) implements MultiAgentIdeAssertion {}

    /**
     * Assertion for child node count.
     */
    @Builder
    public record ChildNodeCountAssertion(
            String nodeId,
            int expectedChildCount,
            String assertionType
    ) implements MultiAgentIdeAssertion {}

    /**
     * Assertion for node completion status.
     */
    @Builder
    public record CompletionStatusAssertion(
            String nodeId,
            int expectedCompletionPercentage,
            String assertionType
    ) implements MultiAgentIdeAssertion {}

    /**
     * Assertion for event count validation.
     */
    @Builder
    public record EventCountAssertion(
            String eventType,
            int expectedEventCount,
            String assertionType
    ) implements MultiAgentIdeAssertion {}

    /**
     * Assertion for unique node IDs.
     */
    @Builder
    public record UniqueNodeIdAssertion(
            int expectedOrchestratorCount,
            boolean shouldHaveDifferentIds,
            String assertionType
    ) implements MultiAgentIdeAssertion {}

    /**
     * Assertion for database persistence of multiple nodes.
     */
    @Builder
    public record DatabasePersistenceAssertion(
            int expectedNodeCount,
            String database,
            boolean shouldBeSeparateEntries
    ) implements MultiAgentIdeAssertion {}

    /**
     * Assertion for protocol-specific event reception.
     */
    @Builder
    public record ProtocolReceptionAssertion(
            String eventType,
            String protocol,
            boolean shouldBeReceived
    ) implements MultiAgentIdeAssertion {}

    /**
     * Assertion for event data deserialization.
     */
    @Builder
    public record EventDeserializationAssertion(
            String eventType,
            boolean shouldDeserializeCorrectly,
            String assertionType
    ) implements MultiAgentIdeAssertion {}

    /**
     * Assertion for node ID matching.
     */
    @Builder
    public record NodeIdMatchAssertion(
            String expectedNodeId,
            String assertionType
    ) implements MultiAgentIdeAssertion {}

    /**
     * Assertion for message bus event publication.
     */
    @Builder
    public record MessageBusPublicationAssertion(
            String eventType,
            String publishDestination,
            boolean shouldBePublished
    ) implements MultiAgentIdeAssertion {}

    /**
     * Assertion for event content validation.
     */
    @Builder
    public record EventContentAssertion(
            String eventType,
            String nodeId,
            String[] expectedFields,
            boolean shouldContainAllFields
    ) implements MultiAgentIdeAssertion {}

    /**
     * Assertion for node status change events.
     */
    @Builder
    public record NodeStatusChangedEventAssertion(
            String eventType,
            String nodeId,
            String newStatus,
            String fromStatus,
            boolean shouldBeReceived
    ) implements MultiAgentIdeAssertion {}

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
    private final List<MultiAgentIdeDataDepCtx.UiEventObservation> uiEvents = new ArrayList<>();
    private final Map<String, Object> assertionResults = new HashMap<>();
    private MultiAgentIdeDataDepCtx.EventQueue eventQueueFromDataDep;
    
    // ============ ASSERTION LIFECYCLE MANAGEMENT ============
    // Pending, executed, and failed assertions for goal_and_graph_initialization and other features
    private final List<MultiAgentIdeAssertion> pendingAssertions = new ArrayList<>();
    private final List<MultiAgentIdeAssertion> executedAssertions = new ArrayList<>();
    private final List<MultiAgentIdeAssertion> failedAssertions = new ArrayList<>();

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

    public void setUiEvents(List<MultiAgentIdeDataDepCtx.UiEventObservation> events) {
        uiEvents.clear();
        uiEvents.addAll(events);
    }

    public List<MultiAgentIdeDataDepCtx.UiEventObservation> getUiEvents() {
        return new ArrayList<>(uiEvents);
    }

    public void putAssertionResult(String key, Object result) {
        assertionResults.put(key, result);
    }

    public Object getAssertionResult(String key) {
        return assertionResults.get(key);
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

    // ============ ASSERTION MANAGEMENT METHODS ============
    // These methods manage the lifecycle of MultiAgentIdeAssertion types
    // Used by step definitions to add assertions and by assertion nodes to execute them

    /**
     * Add an assertion to the pending assertions list.
     * Called by step definitions to register assertions that will be validated.
     * @param assertion The assertion to add
     */
    public void addPendingAssertion(MultiAgentIdeAssertion assertion) {
        pendingAssertions.add(assertion);
    }

    /**
     * Get all pending assertions that have not yet been executed.
     * @return List of pending assertions
     */
    public List<MultiAgentIdeAssertion> getPendingAssertions() {
        return new ArrayList<>(pendingAssertions);
    }

    /**
     * Get the count of pending assertions.
     * @return Number of pending assertions
     */
    public int getPendingAssertionCount() {
        return pendingAssertions.size();
    }

    /**
     * Mark an assertion as executed after validation passes.
     * Moves assertion from pending to executed list.
     * @param assertion The assertion that was executed
     */
    public void markAssertionExecuted(MultiAgentIdeAssertion assertion) {
        pendingAssertions.remove(assertion);
        executedAssertions.add(assertion);
    }

    /**
     * Mark an assertion as failed when validation fails.
     * Moves assertion from pending to failed list.
     * @param assertion The assertion that failed
     */
    public void markAssertionFailed(MultiAgentIdeAssertion assertion) {
        pendingAssertions.remove(assertion);
        failedAssertions.add(assertion);
    }

    /**
     * Get all executed assertions that passed validation.
     * @return List of executed assertions
     */
    public List<MultiAgentIdeAssertion> getExecutedAssertions() {
        return new ArrayList<>(executedAssertions);
    }

    /**
     * Get all failed assertions that did not pass validation.
     * @return List of failed assertions
     */
    public List<MultiAgentIdeAssertion> getFailedAssertions() {
        return new ArrayList<>(failedAssertions);
    }

    /**
     * Clear all assertions (pending, executed, and failed).
     * Called during test context reset.
     */
    public void clearAllAssertions() {
        pendingAssertions.clear();
        executedAssertions.clear();
        failedAssertions.clear();
    }

    /**
     * Check if there are any assertions to validate.
     * @return true if there are pending assertions
     */
    public boolean hasAssertionsToValidate() {
        return !pendingAssertions.isEmpty();
    }

    /**
     * Check if all assertions have passed (no failures, all executed).
     * @return true if all assertions passed
     */
    public boolean allAssertionsPassed() {
        return pendingAssertions.isEmpty() && failedAssertions.isEmpty();
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
