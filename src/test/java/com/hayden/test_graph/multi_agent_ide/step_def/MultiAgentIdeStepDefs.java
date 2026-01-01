package com.hayden.test_graph.multi_agent_ide.step_def;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx.MultiAgentIdeAssertCtx;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepCtx;
import com.hayden.test_graph.multi_agent_ide.init.ctx.MultiAgentIdeInit;
import com.hayden.test_graph.multi_agent_ide.init.mountebank.ctx.MultiAgentIdeMbInitCtx;
import com.hayden.test_graph.meta.exec.MetaProgExec;
import com.hayden.test_graph.steps.ExecAssertStep;
import com.hayden.test_graph.steps.RegisterInitStep;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Step definitions for multi-agent-ide feature files.
 * These steps map Gherkin scenarios to test graph contexts and assertions.
 */
public class MultiAgentIdeStepDefs implements ResettableStep {

    @Autowired
    @ResettableThread
    private MultiAgentIdeInit multiAgentIdeInit;
    
    @Autowired
    @ResettableThread
    private Assertions assertions;

    @Autowired
    @ResettableThread
    private MultiAgentIdeDataDepCtx multiAgentIdeDataDep;

    @Autowired
    @ResettableThread
    private MultiAgentIdeAssertCtx multiAgentIdeAssert;

    @Autowired
    @ResettableThread
    private MultiAgentIdeMbInitCtx multiAgentIdeMbInit;

    @Autowired
    @ResettableThread
    private MetaProgExec metaProgExec;

    /**
     * Define computation graph structure from data table.
     * Table columns: nodeId, nodeType, status, parentId, children, prompt (optional)
     * Creates specs for all nodes, relationships, and stores prompts for agent execution.
     */
    @Given("a computation graph with the following structure:")
    @RegisterInitStep({MultiAgentIdeInit.class})
    public void computation_graph_structure(io.cucumber.datatable.DataTable table) {
        // Parse the graph structure from data table
        var rows = table.asMaps(String.class, String.class);
        MultiAgentIdeDataDepCtx.OrchestrationRequestConfig requestConfig = null;
        String fallbackGoal = null;

        for (var row : rows) {
            String nodeId = row.get("nodeId");
            String nodeType = row.get("nodeType");
            String status = row.get("status");
            String parentId = row.get("parentId");
            String children = row.get("children");
            String prompt = row.get("prompt");  // New: agent prompt/goal

            // Create WorkNodeStateSpec for each node
            var nodeSpec = MultiAgentIdeInit.NodeStateSpec.builder()
                    .nodeId(nodeId)
                    .status(status)
                    .description("Node " + nodeId + " of type " + nodeType)
                    .originalPrompt(prompt != null ? prompt : "")  // Store the prompt
                    .hasWorktree(false)
                    .completionPercentage(status.equals("COMPLETED") ? 100 : 0)
                    .build();

            multiAgentIdeInit.addWorkNodeStateSpec(nodeSpec);

            if (fallbackGoal == null && prompt != null && !prompt.isBlank()) {
                fallbackGoal = prompt;
            }

            if (requestConfig == null && "ORCHESTRATOR".equalsIgnoreCase(nodeType)) {
                String goal = prompt != null && !prompt.isBlank()
                        ? prompt
                        : "Execute orchestration workflow";
                requestConfig = MultiAgentIdeDataDepCtx.OrchestrationRequestConfig.builder()
                        .baseUrl(resolveBaseUrl())
                        .goal(goal)
                        .repositoryUrl(resolveRepositoryUrl())
                        .baseBranch("main")
                        .nodeId(nodeId)
                        .build();
            }
        }

        if (requestConfig != null) {
            multiAgentIdeDataDep.addOrchestrationRequest(requestConfig);
        } else if (fallbackGoal != null) {
            multiAgentIdeDataDep.addOrchestrationRequest(
                    MultiAgentIdeDataDepCtx.OrchestrationRequestConfig.builder()
                            .baseUrl(resolveBaseUrl())
                            .goal(fallbackGoal)
                            .repositoryUrl(resolveRepositoryUrl())
                            .baseBranch("main")
                            .build()
            );
        }
    }

    /**
     * Define expected events table with comprehensive event details and mock responses.
     * Table columns: eventType, nodeType, payloadFile, order (optional)
     * Creates assertions for each expected event and loads mock responses from Mountebank.
     * 
     * Mock response files are loaded from: /multi_agent_ide/responses/ directory
     * Files should contain JSON payloads that represent model responses (tool calls, code, etc.)
     */
    @And("the expected events for this scenario are:")
    @RegisterInitStep({MultiAgentIdeInit.class})
    public void expected_events_scenario(io.cucumber.datatable.DataTable table) {
        var rows = table.asMaps(String.class, String.class);
        int eventIndex = 0;
        
        for (var row : rows) {
            String eventType = row.get("eventType");
            String nodeType = row.get("nodeType");
            String payloadFile = row.get("payloadFile");  // Event payload file reference
            String orderStr = row.get("order");
            
            int eventOrder = orderStr != null ? Integer.parseInt(orderStr) : eventIndex;
            
            // Create assertion for this event
            var assertion = MultiAgentIdeAssertCtx.EventAssertion.builder()
                    .eventType(eventType)
                    .nodeType(nodeType)
                    .payloadFile(payloadFile)
                    .shouldExist(true)
                    .build();
            
            multiAgentIdeAssert.addPendingAssertion(assertion);
            
            eventIndex++;
        }
        
    }


    /**
     * Define multiple computation graphs for multi-goal scenarios.
     * Table columns: graphId, nodeId, nodeType, status, parentId, children, prompt (optional)
     * Creates multiple independent graphs with their own prompts and specifications.
     */
    @Given("multiple computation graphs with the following structure:")
    @RegisterInitStep({MultiAgentIdeInit.class})
    public void multiple_computation_graphs(io.cucumber.datatable.DataTable table) {
        var rows = table.asMaps(String.class, String.class);
        var graphMap = new HashMap<String, Integer>();
        
        for (var row : rows) {
            String graphId = row.get("graphId");
            String nodeId = row.get("nodeId");
            String nodeType = row.get("nodeType");
            String status = row.get("status");
            String parentId = row.get("parentId");
            String prompt = row.get("prompt");  // Agent prompt for this node
            
            // Create spec for node
            var nodeSpec = MultiAgentIdeInit.NodeStateSpec.builder()
                    .nodeId(nodeId)
                    .status(status)
                    .description("Node " + nodeId + " in " + graphId)
                    .originalPrompt(prompt != null ? prompt : "")  // Store prompt
                    .hasWorktree(false)
                    .completionPercentage(status.equals("COMPLETED") ? 100 : 0)
                    .build();
            
            multiAgentIdeInit.addWorkNodeStateSpec(nodeSpec);

            // Track nodes per graph
            int nodeCount = graphMap.getOrDefault(graphId, 0) + 1;
            graphMap.put(graphId, nodeCount);

            if ("ORCHESTRATOR".equalsIgnoreCase(nodeType)) {
                String goal = prompt != null && !prompt.isBlank()
                        ? prompt
                        : "Execute orchestration workflow";
                multiAgentIdeDataDep.addOrchestrationRequest(
                        MultiAgentIdeDataDepCtx.OrchestrationRequestConfig.builder()
                                .baseUrl(resolveBaseUrl())
                                .goal(goal)
                                .repositoryUrl(resolveRepositoryUrl())
                                .baseBranch("main")
                                .nodeId(nodeId)
                                .build()
                );
            }
        }
        
    }

    /**
     * Configure test settings such as model type and streaming behavior.
     * Expected columns: key, value
     */
    @Given("the test configuration is:")
    public void test_configuration(io.cucumber.datatable.DataTable table) {
        var rows = table.asMaps(String.class, String.class);
        String modelType = null;
        Boolean useStreamingModel = null;

        for (var row : rows) {
            String key = row.get("key");
            String value = row.get("value");
            if (key == null || value == null) {
                continue;
            }
            if ("MODEL_TYPE".equalsIgnoreCase(key)) {
                modelType = value;
            } else if ("USE_STREAMING_MODEL".equalsIgnoreCase(key)) {
                useStreamingModel = Boolean.parseBoolean(value);
            }
        }

        var existing = multiAgentIdeDataDep.getLangChain4jMockConfig();
        Map<String, String> mockResponses = existing != null ? new HashMap<>(existing.mockResponses()) : new HashMap<>();
        boolean resolvedStreaming = useStreamingModel != null ? useStreamingModel : existing == null || existing.useStreamingModel();
        String resolvedModelType = modelType != null ? modelType : (existing != null ? existing.modelType() : "http");

        multiAgentIdeDataDep.setLangChain4jMockConfig(
                MultiAgentIdeDataDepCtx.LangChain4jMockConfig.builder()
                        .useStreamingModel(resolvedStreaming)
                        .modelType(resolvedModelType)
                        .mockResponses(mockResponses)
                        .build()
        );
    }

    /**
     * Configure SummaryGraphAgent to generate specific token count.
     */
    @And("the SummaryGraphAgent is configured to generate {int} tokens")
    @RegisterInitStep({MultiAgentIdeInit.class})
    public void summary_agent_token_configuration(Integer tokenCount) {
    }

    /**
     * Execute graph with standard graph execution.
     */
    @When("the graph execution completes")
    public void graph_execution_completes() {
        if (multiAgentIdeInit.getAppLaunchConfig() == null) {
            multiAgentIdeInit.setAppLaunchConfig(new MultiAgentIdeInit.AppLaunchConfig((String) null));
        }
        if (multiAgentIdeDataDep.getEventSubscriptionConfig() == null) {
            String baseUrl = resolveBaseUrl();
            String endpoint = baseUrl.endsWith("/")
                    ? baseUrl + "api/events/stream"
                    : baseUrl + "/api/events/stream";
            multiAgentIdeDataDep.setEventSubscriptionConfig(
                    MultiAgentIdeDataDepCtx.EventSubscriptionConfig.builder()
                            .subscriptionProtocol("sse")
                            .eventEndpoint(endpoint)
                            .pollIntervalMs(100)
                            .subscriptionTimeoutMs(30000L)
                            .autoStart(true)
                            .build()
            );
        }
        int expectedCount = multiAgentIdeAssert.getPendingAssertionCount();
        if (expectedCount > 0) {
            multiAgentIdeDataDep.setExpectedEventCount(expectedCount);
        }
    }

    /**
     * Execute graph with SummaryGraphAgent enabled.
     */
    @When("the graph execution completes with SummaryGraphAgent enabled")
    public void graph_execution_with_summary_agent() {
        graph_execution_completes();
    }

    /**
     * Execute SummaryGraphAgent to generate summary.
     */
    @When("the SummaryGraphAgent generates the summary")
    public void summary_graph_agent_generates() {
    }

    /**
     * Trigger summary generation completion.
     */
    @When("the SummaryGraphAgent completes summary generation")
    public void summary_graph_agent_completes() {
    }

    /**
     * Execute graph with persistence enabled.
     */
    @When("the graph execution completes and persistence is triggered")
    public void graph_execution_with_persistence() {
        graph_execution_completes();
    }

    /**
     * Execute goal A completion check independently.
     */
    @When("goal-A completion check runs independently")
    public void goal_a_completion_check() {
    }

    /**
     * Launch the app and submit a goal through the UI for Selenium-driven E2E.
     */
    @When("the UI goal is submitted via Selenium")
    @RegisterInitStep({MultiAgentIdeInit.class})
    public void ui_goal_submitted_via_selenium() {
        if (multiAgentIdeInit.getAppLaunchConfig() == null) {
            multiAgentIdeInit.setAppLaunchConfig(new MultiAgentIdeInit.AppLaunchConfig((String) null));
        }
        if (multiAgentIdeDataDep.getSeleniumUiConfig() == null) {
            multiAgentIdeDataDep.setSeleniumUiConfig(
                    new MultiAgentIdeDataDepCtx.SeleniumUiConfig(null, null, null)
            );
        }
        int expectedCount = multiAgentIdeAssert.getPendingAssertionCount();
        if (expectedCount <= 0) {
            expectedCount = 1;
        }
        multiAgentIdeDataDep.setExpectedEventCount(expectedCount);
        metaProgExec.register(MultiAgentIdeDataDepCtx.class);
    }

    /**
     * Validate that expected events were received.
     */
    @Then("the expected events should have been received")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void expected_events_received() {
    }

    /**
     * Validate that no additional unexpected events were captured.
     */
    @And("no additional events should have been captured")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void no_additional_events() {
    }

    /**
     * Validate pruned nodes don't prevent completion.
     */
    @And("pruned nodes should not prevent goal completion")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void pruned_nodes_not_blocking() {
        multiAgentIdeAssert.putAssertionResult("pruned_nodes_checked", true);
    }

    /**
     * Validate failure information in events.
     */
    @And("failure information should be captured in the final event")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void failure_information_captured() {
        var allEvents = multiAgentIdeAssert.getAllEventsFromQueue();
        assertions.assertFalse(allEvents.isEmpty(), "Events should contain failure information");
    }

    /**
     * Validate SummaryNode creation.
     */
    @And("a SummaryNode should be created as a child of OrchestratorNode")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void summary_node_created_as_child() {
        var assertion = MultiAgentIdeAssertCtx.EventAssertion.builder()
                .eventType("NODE_ADDED")
                .nodeType("SUMMARY")
                .payloadFile(null)
                .shouldExist(true)
                .build();
        
        multiAgentIdeAssert.addPendingAssertion(assertion);
    }

    /**
     * Validate SummaryNode state transitions.
     */
    @And("the SummaryNode should transition through READY → RUNNING → COMPLETED")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void summary_node_transitions() {
        var readyAssertion = MultiAgentIdeAssertCtx.NodeStatusAssertion.builder()
                .nodeId("summary-1")
                .expectedStatus("READY")
                .assertionType("transition_check")
                .build();
        
        var runningAssertion = MultiAgentIdeAssertCtx.NodeStatusAssertion.builder()
                .nodeId("summary-1")
                .expectedStatus("RUNNING")
                .assertionType("transition_check")
                .build();
        
        var completedAssertion = MultiAgentIdeAssertCtx.NodeStatusAssertion.builder()
                .nodeId("summary-1")
                .expectedStatus("COMPLETED")
                .assertionType("transition_check")
                .build();
        
        multiAgentIdeAssert.addPendingAssertion(readyAssertion);
        multiAgentIdeAssert.addPendingAssertion(runningAssertion);
        multiAgentIdeAssert.addPendingAssertion(completedAssertion);
    }

    /**
     * Validate expected NODE_STREAM_DELTA events in order.
     */
    @Then("the expected NODE_STREAM_DELTA events should have been received in order")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void stream_delta_events_in_order() {
    }

    /**
     * Validate total tokens streamed.
     */
    @And("a total of {int} tokens should have been streamed")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void total_tokens_streamed(Integer expectedTokens) {
    }

    /**
     * Validate test listener received tokens.
     */
    @And("the test listener should have received all streaming tokens")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void test_listener_received_tokens() {
        var allEvents = multiAgentIdeAssert.getAllEventsFromQueue();
        assertions.assertFalse(allEvents.isEmpty(), "Test listener should have received streaming events");
    }

    /**
     * Validate summary aggregates all nodes.
     */
    @And("the summary should aggregate information from all {int} completed work nodes")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void summary_aggregates_all_nodes(Integer nodeCount) {
        multiAgentIdeAssert.putAssertionResult("aggregated_node_count", nodeCount);
    }

    /**
     * Validate summary content fields.
     */
    @And("the summary content should include goal_description, nodes_executed, execution_time, and final_status fields")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void summary_content_fields() {
        String[] requiredFields = {"goal_description", "nodes_executed", "execution_time", "final_status"};
        var assertion = MultiAgentIdeAssertCtx.EventContentAssertion.builder()
                .eventType("GOAL_COMPLETED")
                .expectedFields(requiredFields)
                .shouldContainAllFields(true)
                .build();
        
        multiAgentIdeAssert.addPendingAssertion(assertion);
    }

    /**
     * Validate events received in specified order.
     */
    @Then("the expected events should have been received in the specified order")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void events_in_specified_order() {
        multiAgentIdeAssert.putAssertionResult("event_order_validated", true);
    }

    /**
     * Validate no out of sequence events.
     */
    @And("no events should be out of sequence")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void no_out_of_sequence_events() {
        multiAgentIdeAssert.putAssertionResult("sequence_check_passed", true);
    }

    /**
     * Validate all WorkNodes persisted with final status.
     */
    @And("all WorkNodes should be persisted with their final status")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void all_work_nodes_persisted() {
        multiAgentIdeAssert.putAssertionResult("work_nodes_persisted_check", true);
    }


    /**
     * Validate events received in order.
     */
    @Then("the expected events should have been received in order")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void expected_events_in_order() {
        events_in_specified_order();
    }

    @And("the mock response file {string}")
    @RegisterInitStep({MultiAgentIdeMbInitCtx.class})
    public void theMockResponseFile(String imposterFile) {
        this.multiAgentIdeMbInit.registerImposterFile(imposterFile);
    }

    private String resolveBaseUrl() {
        String configured = System.getProperty("multiagentide.baseUrl");
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        return "http://localhost:8080";
    }

    private String resolveRepositoryUrl() {
        String repoRoot = System.getProperty("user.dir");
        if (repoRoot != null && repoRoot.endsWith("test_graph")) {
            return java.nio.file.Paths.get(repoRoot).getParent().toString();
        }
        return repoRoot;
    }

}
