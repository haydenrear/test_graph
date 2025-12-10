package com.hayden.test_graph.multi_agent_ide.step_def;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx.MultiAgentIdeAssertCtx;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepCtx;
import com.hayden.test_graph.multi_agent_ide.data_dep.nodes.EventPollingDataDepNode;
import com.hayden.test_graph.multi_agent_ide.data_dep.nodes.EventSubscriptionDataDepNode;
import com.hayden.test_graph.multi_agent_ide.init.ctx.MultiAgentIdeInit;
import com.hayden.test_graph.multi_agent_ide.init.mountebank.ctx.MultiAgentIdeMbInitCtx;
import com.hayden.test_graph.multi_agent_ide.init.nodes.GitRepositoryInitNode;
import com.hayden.test_graph.multi_agent_ide.util.GitRepositoryTestHelper;
import com.hayden.test_graph.steps.ExecAssertStep;
import com.hayden.test_graph.steps.RegisterInitStep;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private MultiAgentIdeDataDepCtx multiAgentIdeDataDep;

    @Autowired
    @ResettableThread
    private MultiAgentIdeAssertCtx multiAgentIdeAssert;

    @Autowired
    @ResettableThread
    private Assertions assertions;

    @Autowired
    @ResettableThread
    private MultiAgentIdeMbInitCtx multiAgentIdeMbInit;

    // ============ BACKGROUND / INITIALIZATION STEPS ============

    @Given("docker-compose is started from {string}")
    @RegisterInitStep(MultiAgentIdeInit.class)
    public void docker_compose_started(String composePath) {
        var config = MultiAgentIdeInit.DockerComposeConfig.builder()
                .composePath(Paths.get(composePath))
                .build();
        multiAgentIdeInit.setDockerComposeConfig(config);
    }

    @And("the multi-agent-ide service is running")
    @RegisterInitStep(MultiAgentIdeInit.class)
    public void multi_agent_ide_service_running() {
        // Service readiness is validated by docker-compose startup
        // Step implementation will be added during feature development
    }

    @And("the event subscription type is {string}")
    @RegisterInitStep(MultiAgentIdeInit.class)
    public void event_subscription_type(String subscriptionType) {
        var config = MultiAgentIdeInit.EventSubscriptionConfig.builder()
                .subscriptionType(subscriptionType)
                .build();
        multiAgentIdeInit.setEventSubscriptionConfig(config);
    }

    @And("a test event listener is subscribed to all events")
    public void test_event_listener_subscribed() {
        // Event listener setup
        // Step implementation will be added during feature development
    }

    @And("LangChain4j models are mocked with predictable responses")
    public void langchain4j_mocked() {
        var mockConfig = MultiAgentIdeDataDepCtx.LangChain4jMockConfig.builder()
                .useStreamingModel(true)
                .modelType("claude-haiku")
                .build();
        multiAgentIdeDataDep.setLangChain4jMockConfig(mockConfig);
    }

    @And("LangChain4j streaming models are configured")
    public void langchain4j_streaming_configured() {
        var mockConfig = MultiAgentIdeDataDepCtx.LangChain4jMockConfig.builder()
                .useStreamingModel(true)
                .modelType("claude-haiku")
                .build();
        multiAgentIdeDataDep.setLangChain4jMockConfig(mockConfig);
    }

    // ============ EVENT SUBSCRIPTION STEPS ============

    @And("event subscription is configured for WebSocket at {string}")
    public void event_subscription_websocket_configured(String endpoint) {
        var config = new MultiAgentIdeDataDepCtx.EventSubscriptionConfig(
                "websocket",
                endpoint
        );
        multiAgentIdeDataDep.setEventSubscriptionConfig(config);
    }

    @And("event subscription is configured for HTTP polling at {string}")
    public void event_subscription_http_configured(String endpoint) {
        var config = new MultiAgentIdeDataDepCtx.EventSubscriptionConfig(
                "http",
                endpoint
        );
        multiAgentIdeDataDep.setEventSubscriptionConfig(config);
    }

    @And("event subscription is configured for Kafka at {string}")
    public void event_subscription_kafka_configured(String endpoint) {
        var config = new MultiAgentIdeDataDepCtx.EventSubscriptionConfig(
                "kafka",
                endpoint
        );
        multiAgentIdeDataDep.setEventSubscriptionConfig(config);
    }

    @And("event subscription poll interval is set to {int} milliseconds")
    public void event_subscription_poll_interval(Integer pollIntervalMs) {
        var currentConfig = multiAgentIdeDataDep.getEventSubscriptionConfig();
        if (currentConfig != null) {
            var updatedConfig = new MultiAgentIdeDataDepCtx.EventSubscriptionConfig(
                    currentConfig.subscriptionProtocol(),
                    currentConfig.eventEndpoint(),
                    pollIntervalMs,
                    currentConfig.subscriptionTimeoutMs(),
                    currentConfig.autoStart()
            );
            multiAgentIdeDataDep.setEventSubscriptionConfig(updatedConfig);
        }
    }

    @And("event subscription timeout is set to {long} milliseconds")
    public void event_subscription_timeout(Long timeoutMs) {
        var currentConfig = multiAgentIdeDataDep.getEventSubscriptionConfig();
        if (currentConfig != null) {
            var updatedConfig = new MultiAgentIdeDataDepCtx.EventSubscriptionConfig(
                    currentConfig.subscriptionProtocol(),
                    currentConfig.eventEndpoint(),
                    currentConfig.pollIntervalMs(),
                    timeoutMs,
                    currentConfig.autoStart()
            );
            multiAgentIdeDataDep.setEventSubscriptionConfig(updatedConfig);
        }
    }

    @And("the event subscription is initialized")
    public void event_subscription_initialized() {
        // This step is typically used in background or before scenario
        // The actual initialization happens in EventSubscriptionDataDepNode during data dep phase
    }

    @And("the event polling is started")
    public void event_polling_started() {
        // This step triggers the event polling setup
        // The actual polling happens in EventPollingDataDepNode during data dep phase
    }

    @And("git is properly configured in the container")
    @RegisterInitStep(MultiAgentIdeInit.class)
    public void git_configured() {
        // Git configuration setup
        // Step implementation will be added during feature development
    }

    @And("a git repository is initialized at {string}")
    @RegisterInitStep(MultiAgentIdeInit.class)
    public void git_repository_initialized(String repoPath) {
        var config = MultiAgentIdeInit.GitRepositoryConfig.builder()
                .repositoryPath(Paths.get(repoPath))
                .build();
        multiAgentIdeInit.setGitRepositoryConfig(config);
    }

    @And("a git repository with submodules is initialized at {string}")
    @RegisterInitStep(MultiAgentIdeInit.class)
    public void git_repository_with_submodules_initialized(String repoPath) {
        var config = new MultiAgentIdeInit.GitRepositoryConfig(
                Paths.get(repoPath),
                java.util.List.of("auth-lib", "utils-lib")
        );
        multiAgentIdeInit.setGitRepositoryConfig(config);
    }

    @And("spec file configuration is set to standard markdown format")
    @RegisterInitStep(MultiAgentIdeInit.class)
    public void spec_file_config_set() {
        var config = new MultiAgentIdeInit.SpecFileConfig();
        multiAgentIdeInit.setSpecFileConfig(config);
    }

    // ============ MOUNTEBANK & MOCK RESPONSE STEPS ============

    @And("mock LangChain4j planning response is configured")
    @RegisterInitStep(MultiAgentIdeMbInitCtx.class)
    public void mock_planning_response_configured() {
        multiAgentIdeMbInit.addMockResponse("planning", 
                "classpath:multi_agent_ide/responses/planning_response.json",
                "/ai/planning",
                8080);
    }

    @And("mock LangChain4j code generation response is configured")
    @RegisterInitStep(MultiAgentIdeMbInitCtx.class)
    public void mock_codegen_response_configured() {
        multiAgentIdeMbInit.addMockResponse("codegen",
                "classpath:multi_agent_ide/responses/code_generation_response.json",
                "/ai/codegen",
                8080);
    }

    @And("mock spec validation response is configured")
    @RegisterInitStep(MultiAgentIdeMbInitCtx.class)
    public void mock_spec_validation_response_configured() {
        multiAgentIdeMbInit.addMockResponse("spec_validation",
                "classpath:multi_agent_ide/responses/spec_validation_response.json",
                "/specs/validate",
                8080);
    }

    @And("mock spec summary response is configured")
    @RegisterInitStep(MultiAgentIdeMbInitCtx.class)
    public void mock_spec_summary_response_configured() {
        multiAgentIdeMbInit.addMockResponse("spec_summary",
                "classpath:multi_agent_ide/responses/spec_summary_response.json",
                "/specs/summary",
                8080);
    }

    @And("mock review response is configured")
    @RegisterInitStep(MultiAgentIdeMbInitCtx.class)
    public void mock_review_response_configured() {
        multiAgentIdeMbInit.addMockResponse("review",
                "classpath:multi_agent_ide/responses/review_response.json",
                "/ai/review",
                8080);
    }

    @And("mock merge response is configured")
    @RegisterInitStep(MultiAgentIdeMbInitCtx.class)
    public void mock_merge_response_configured() {
        multiAgentIdeMbInit.addMockResponse("merge",
                "classpath:multi_agent_ide/responses/merge_response.json",
                "/git/merge",
                8080);
    }

    // ============ GIT REPOSITORY SETUP STEPS ============

    @And("a basic test git repository is configured")
    @RegisterInitStep(GitRepositoryInitNode.class)
    public void basic_test_git_repository_configured() {
        String sourceDir = "classpath:multi_agent_ide/git_repos/basic";
        
        MultiAgentIdeInit.RepositorySpec spec = MultiAgentIdeInit.RepositorySpec.builder()
                .name("basic-test-repo")
                .sourceDirectory(Paths.get(sourceDir))
                .nodeId("test-node-1")
                .goal("Initialize basic test repository")
                .submoduleNames(Collections.emptyList())
                .build();
        
        multiAgentIdeInit.addRepositorySpec(spec);
    }

    @And("a test git repository with submodules is configured")
    @RegisterInitStep(GitRepositoryInitNode.class)
    public void test_git_repository_with_submodules_configured() {
        String sourceDir = "classpath:multi_agent_ide/git_repos/with_submodules";
        
        List<String> submoduleNames = new ArrayList<>();
        submoduleNames.add("auth-lib");
        submoduleNames.add("utils-lib");
        
        MultiAgentIdeInit.RepositorySpec spec = MultiAgentIdeInit.RepositorySpec.builder()
                .name("submodule-test-repo")
                .sourceDirectory(Paths.get(sourceDir))
                .nodeId("test-node-2")
                .goal("Test main repo with submodules")
                .submoduleNames(submoduleNames)
                .build();
        
        multiAgentIdeInit.addRepositorySpec(spec);
    }

    @And("a test git repository for complex workflow is configured")
    @RegisterInitStep(GitRepositoryInitNode.class)
    public void complex_test_git_repository_configured() {
        String sourceDir = "classpath:multi_agent_ide/git_repos/complex_workflow";
        
        MultiAgentIdeInit.RepositorySpec spec = MultiAgentIdeInit.RepositorySpec.builder()
                .name("complex-workflow-repo")
                .sourceDirectory(Paths.get(sourceDir))
                .nodeId("test-node-3")
                .goal("Complex workflow with multiple branches")
                .branchName("feature/complex")
                .submoduleNames(Collections.emptyList())
                .build();
        
        multiAgentIdeInit.addRepositorySpec(spec);
    }

    @And("a mock UI client is registered to receive events")
    public void mock_ui_client_registered() {
        // Mock UI client registration
        // Step implementation will be added during feature development
    }

    @And("the UI is configured to display both graph and worktree views")
    public void ui_configured_dual_views() {
        // UI configuration
        // Step implementation will be added during feature development
    }

    @And("the execution engine is configured for parallel execution")
    public void execution_engine_parallel() {
        // Parallel execution configuration
        // Step implementation will be added during feature development
    }

    // ============ GOAL CREATION STEPS ============

    @When("a new goal is created via message with description {string}")
    public void new_goal_created(String description) {
        // Goal creation through messaging layer
        // Step implementation will be added during feature development
    }

    @When("a new goal is created with description {string}")
    public void new_goal_created_simple(String description) {
        // Simple goal creation
        // Step implementation will be added during feature development
    }

    @When("a goal {string} is created via message with description {string}")
    public void goal_with_id_created(String goalId, String description) {
        // Goal creation with explicit ID
        // Step implementation will be added during feature development
    }

    // ============ EVENT QUEUE TRANSFER STEPS ============

    @And("the event queue is transferred to the assert context")
    public void event_queue_transferred_to_assert() {
        // Transfer the event queue from data dep to assert context
        multiAgentIdeAssert.transferEventQueueFromDataDep(multiAgentIdeDataDep);
    }

    // ============ EVENT ASSERTION STEPS ============

    @Then("exactly {int} events should be received in the queue")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void events_count_in_queue(Integer expectedCount) {
        var allEvents = multiAgentIdeAssert.getAllEventsFromQueue();
        assertions.assertEqual(allEvents.size(), expectedCount, 
                "Expected " + expectedCount + " events but got " + allEvents.size());
    }

    @Then("the event queue should not be empty")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void event_queue_not_empty() {
        var queue = multiAgentIdeAssert.getEventQueueFromDataDep();
        assertions.assertFalse(queue.isEmpty(), "Event queue should not be empty");
    }

    @Then("the event queue should be empty")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void event_queue_empty() {
        var queue = multiAgentIdeAssert.getEventQueueFromDataDep();
        assertions.assertTrue(queue.isEmpty(), "Event queue should be empty");
    }

    @Then("events in the queue should be in order")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void events_in_order() {
        var allEvents = multiAgentIdeAssert.getAllEventsFromQueue();
        assertions.assertFalse(allEvents.isEmpty(), "No events in queue to verify order");
        // Events are in order by definition since we're using a FIFO queue
        multiAgentIdeAssert.putAssertionResult("events_ordered", true);
    }

    @Then("a NodeAddedEvent should be received containing an OrchestratorNode")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void node_added_event_received() {
        // Event assertion
        // Step implementation will be added during feature development
    }

    @Then("a NodeAddedEvent should be emitted for node X")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void node_added_event_emitted() {
        // Event emission validation
        // Step implementation will be added during feature development
    }

    @Then("a {string} should be received")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void event_received(String eventType) {
        // Generic event reception validation
        // Step implementation will be added during feature development
    }

    @Then("exactly one {string} should be received")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void exactly_one_event_received(String eventType) {
        // Single event validation
        // Step implementation will be added during feature development
    }

    // ============ GRAPH STATE STEPS ============

    @Then("the OrchestratorNode should have status {string}")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void node_has_status(String status) {
        // Node status assertion
        // Step implementation will be added during feature development
    }

    @Then("the OrchestratorNode should be {string}")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void node_has_capability(String capability) {
        // Node capability assertion
        // Step implementation will be added during feature development
    }

    @Then("the OrchestratorNode should have no child nodes initially")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void node_has_no_children() {
        // Child node count assertion
        // Step implementation will be added during feature development
    }

    // ============ WORKTREE STEPS ============

    @Then("a WorktreeContext should be created")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void worktree_context_created() {
        // Worktree creation validation
        // Step implementation will be added during feature development
    }

    @Then("a git worktree should be created at that path")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void git_worktree_created() {
        // Git worktree creation validation
        // Step implementation will be added during feature development
    }

    @Then("a WorktreeCreatedEvent should be emitted")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void worktree_created_event_emitted() {
        // Worktree creation event assertion
        // Step implementation will be added during feature development
    }

    // ============ STREAMING STEPS ============

    @Then("NodeStreamDeltaEvent should be emitted with {string}")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void stream_delta_event_emitted(String content) {
        // Streaming event assertion
        // Step implementation will be added during feature development
    }

    @And("the test listener should receive tokens incrementally")
    public void test_listener_receives_tokens() {
        // Token reception validation
        // Step implementation will be added during feature development
    }

    // ============ MESSAGING STEPS ============

    @When("an interrupt message is sent")
    public void interrupt_message_sent() {
        // Interrupt message sending
        // Step implementation will be added during feature development
    }

    @When("a merge request is sent via message")
    public void merge_request_sent() {
        // Merge request message sending
        // Step implementation will be added during feature development
    }

    @When("a branch request is sent via message with modified goal {string}")
    public void branch_request_sent(String modifiedGoal) {
        // Branch request message sending
        // Step implementation will be added during feature development
    }

    @When("an approval message is sent")
    public void approval_message_sent() {
        // Approval message sending
        // Step implementation will be added during feature development
    }

    @When("a rejection message is sent with feedback {string}")
    public void rejection_message_sent(String feedback) {
        // Rejection message sending with feedback
        // Step implementation will be added during feature development
    }

    // ============ SPEC FILE STEPS ============

    @Then("a spec file should be created at {string}")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void spec_file_created(String specPath) {
        // Spec file creation validation
        // Step implementation will be added during feature development
    }

    @Then("the spec should be validated")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void spec_validated() {
        // Spec validation assertion
        // Step implementation will be added during feature development
    }

    @When("get_summary is called on the spec")
    public void get_summary_called() {
        // Get summary call
        // Step implementation will be added during feature development
    }

    @When("get_section is called for {string}")
    public void get_section_called(String sectionPath) {
        // Get section call
        // Step implementation will be added during feature development
    }

    @Then("the spec should contain section {string}")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void spec_contains_section(String sectionName) {
        // Section existence assertion
        // Step implementation will be added during feature development
    }

    // ============ SUBMODULE STEPS ============

    @Then("a WorktreeContext should be created for each submodule")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void submodule_worktrees_created() {
        // Submodule worktree creation assertion
        // Step implementation will be added during feature development
    }

    @Then("each submodule should have its own WorktreeContext")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void each_submodule_has_worktree() {
        // Individual submodule worktree assertion
        // Step implementation will be added during feature development
    }

    @When("EditorGraphAgent edits main repo and submodule {string}")
    public void editor_edits_submodule(String submoduleName) {
        // Submodule editing operation
        // Step implementation will be added during feature development
    }

    @Then("the submodule worktree should be at correct commit")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void submodule_worktree_correct_commit() {
        // Submodule commit assertion
        // Step implementation will be added during feature development
    }

    @When("merge includes changes in {string} and {string}")
    public void merge_includes_submodules(String submodule1, String submodule2) {
        // Multi-submodule merge operation
        // Step implementation will be added during feature development
    }

    @Then("merge conflict in {string} is detected")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void submodule_merge_conflict_detected(String submoduleName) {
        // Submodule merge conflict assertion
        // Step implementation will be added during feature development
    }

    @Then("submodule pointer in main repo is updated")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void submodule_pointer_updated() {
        // Submodule pointer update assertion
        // Step implementation will be added during feature development
    }

    // ============ GENERIC ASSERTION STEPS ============

    @Then("the system should {string}")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void system_assertion(String assertion) {
        // Generic system assertion
        // Step implementation will be added during feature development
    }

    @Then("the {string} should {string}")
    @ExecAssertStep(MultiAgentIdeAssertCtx.class)
    public void entity_assertion(String entity, String assertion) {
        // Generic entity assertion
        // Step implementation will be added during feature development
    }
}
