@multi_agent_ide @completion @summary
Feature: Goal Completion and Result Summarization
  As the orchestration system
  I want to aggregate results when all work is complete
  So that users receive a comprehensive summary of what was accomplished

  Background:
    # Consolidated test environment initialization replacing 4 individual steps
    Given the multi-agent-ide test environment is initialized with:
      | parameter                   | value           |
      | dockerComposePath           | <composePath>   |
      | serviceToStart              | multi-agent-ide |
      | eventSubscriptionType       | websocket       |
      | testEventListenerSubscribed | all_events      |

  @completion @core
  Scenario: Goal completes when all terminal nodes reach completion
    Given a computation graph with the following structure:
      | nodeId       | nodeType     | status | parentId | children | prompt                               |
      | ORCHESTRATOR | ORCHESTRATOR | READY  | null     | WORK     | Build a REST API for user management |
    And the expected events for this scenario are:
      | eventType           | nodeType     | targetNodeId | payloadFile                      |
      | NODE_STATUS_CHANGED | WORK         | null         | work-a-status-changed.json       |
      | NODE_STATUS_CHANGED | WORK         | null         | work-b-status-changed.json       |
      | NODE_STATUS_CHANGED | WORK         | null         | work-c-status-changed.json       |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | null         | orchestrator-status-changed.json |
      | GOAL_COMPLETED      | ORCHESTRATOR | null         | goal-completed.json              |
#    will load matchers and responses
    And the mock response file ""
    When the graph execution completes
    Then the expected events should have been received
    And no additional events should have been captured
    And the overall goal status should be COMPLETED

  @completion @partial
  Scenario: Goal completion handles pruned nodes correctly
    Given a computation graph with the following structure:
      | nodeId       | nodeType     | status | parentId | children | prompt                    |
      | ORCHESTRATOR | ORCHESTRATOR | READY  | null     | WORK     | Build notification system |
    And the expected events for this scenario are:
      | eventType           | nodeType     | targetNodeId | payloadFile                      |
      | NODE_PRUNED         | WORK         | null         | work-c-pruned.json               |
      | NODE_STATUS_CHANGED | WORK         | null         | work-a-status-changed.json       |
      | NODE_STATUS_CHANGED | WORK         | null         | work-b-status-changed.json       |
      | NODE_STATUS_CHANGED | WORK         | null         | work-d-status-changed.json       |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | null         | orchestrator-status-changed.json |
      | GOAL_COMPLETED      | ORCHESTRATOR | null         | goal-completed.json              |
    And the mock response file ""
    When the graph execution completes
    Then the expected events should have been received
    And pruned nodes should not prevent goal completion

  @completion @failure_handling
  Scenario: Goal handles nodes that fail gracefully
    Given a computation graph with the following structure:
      | nodeId       | nodeType     | status | parentId | children | prompt                       |
      | ORCHESTRATOR | ORCHESTRATOR | READY  | null     | WORK     | Build microservices platform |
    And the expected events for this scenario are:
      | eventType           | nodeType     | targetNodeId | payloadFile                       |
      | NODE_STATUS_CHANGED | WORK         | null         | work-a-status-changed.json        |
      | NODE_STATUS_CHANGED | WORK         | null         | work-b-failed.json                |
      | NODE_STATUS_CHANGED | WORK         | null         | work-c-status-changed.json        |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | null         | orchestrator-status-changed.json  |
      | GOAL_COMPLETED      | ORCHESTRATOR | null         | goal-completed-with-failures.json |
    When the graph execution completes
    Then the expected events should have been received
    And the overall goal status should be COMPLETED_WITH_FAILURES
    And failure information should be captured in the final event

  @summary @core
  Scenario: Summary node is created when goal completes
    Given a computation graph with the following structure:
      | nodeId       | nodeType     | status | parentId | children | prompt                  |
      | ORCHESTRATOR | ORCHESTRATOR | READY  | null     | WORK     | Implement data pipeline |
    And the expected events for this scenario are:
      | eventType           | nodeType     | targetNodeId | payloadFile                      |
      | NODE_STATUS_CHANGED | WORK         | null         | work-a-status-changed.json       |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | null         | orchestrator-status-changed.json |
      | NODE_ADDED          | SUMMARY      | ORCHESTRATOR | summary-node-added.json          |
      | NODE_STATUS_CHANGED | SUMMARY      | null         | summary-status-running.json      |
      | NODE_STREAM_DELTA   | SUMMARY      | null         | summary-stream-delta.json        |
      | NODE_STATUS_CHANGED | SUMMARY      | null         | summary-status-completed.json    |
      | GOAL_COMPLETED      | ORCHESTRATOR | null         | goal-completed.json              |
    And the mock response file ""
    When the graph execution completes with SummaryGraphAgent enabled
    Then the expected events should have been received in order
    And a SummaryNode should be created as a child of OrchestratorNode
    And the SummaryNode should transition through READY → RUNNING → COMPLETED

  @summary @streaming
  Scenario: Summary generation streams output like work nodes
    Given a computation graph with the following structure:
      | nodeId       | nodeType     | status | parentId | children | prompt                                |
      | ORCHESTRATOR | ORCHESTRATOR | READY  | null     | WORK     | Optimize database queries and indexes |
    And the SummaryGraphAgent is configured to generate 500 tokens
    And the expected events for this scenario are:
      | eventType         | nodeType | targetNodeId | payloadFile                 |
      | NODE_STREAM_DELTA | SUMMARY  | null         | summary-stream-delta-1.json |
      | NODE_STREAM_DELTA | SUMMARY  | null         | summary-stream-delta-2.json |
      | NODE_STREAM_DELTA | SUMMARY  | null         | summary-stream-delta-3.json |
      | NODE_STREAM_DELTA | SUMMARY  | null         | summary-stream-delta-4.json |
      | NODE_STREAM_DELTA | SUMMARY  | null         | summary-stream-delta-5.json |
    And the mock response file ""
    When the SummaryGraphAgent generates the summary
    Then the expected NODE_STREAM_DELTA events should have been received in order
    And a total of 500 tokens should have been streamed
    And the test listener should have received all streaming tokens

  @summary @structure
  Scenario: Summary contains structured information about completion
    Given a computation graph with the following structure:
      | nodeId       | nodeType     | status | parentId | children | prompt                    |
      | ORCHESTRATOR | ORCHESTRATOR | READY  | null     | WORK     | Build e-commerce platform |
    And the expected events for this scenario are:
      | eventType           | nodeType     | targetNodeId | payloadFile                    |
      | NODE_STREAM_DELTA   | SUMMARY      | null         | summary-plan-section.json      |
      | NODE_STREAM_DELTA   | SUMMARY      | null         | summary-results-section.json   |
      | NODE_STATUS_CHANGED | SUMMARY      | null         | summary-status-completed.json  |
      | GOAL_COMPLETED      | ORCHESTRATOR | null         | goal-completed-structured.json |
    And the mock response file ""
    When the SummaryGraphAgent completes summary generation
    Then the expected events should have been received
    And the summary should aggregate information from all 5 completed work nodes
    And the summary content should include goal_description, nodes_executed, execution_time, and final_status fields

  @completion @event_ordering
  Scenario: Completion events are emitted in correct order
    Given a computation graph with the following structure:
      | nodeId       | nodeType     | status | parentId | children | prompt            |
      | ORCHESTRATOR | ORCHESTRATOR | READY  | null     | WORK     | Create mobile app |
    And the expected events for this scenario are:
      | eventType           | nodeType     | targetNodeId | payloadFile                      |
      | NODE_STATUS_CHANGED | WORK         | null         | work-a-status-changed.json       |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | null         | orchestrator-status-changed.json |
      | NODE_ADDED          | SUMMARY      | ORCHESTRATOR | summary-node-added.json          |
      | NODE_STATUS_CHANGED | SUMMARY      | null         | summary-status-running.json      |
      | NODE_STATUS_CHANGED | SUMMARY      | null         | summary-status-completed.json    |
      | GOAL_COMPLETED      | ORCHESTRATOR | null         | goal-completed.json              |
    And the mock response file ""
    When the graph execution completes
    Then the expected events should have been received in the specified order
    And no events should be out of sequence

  @persistence @database
  Scenario: Completed goal and summary are persisted
    Given a computation graph with the following structure:
      | nodeId       | nodeType     | status    | parentId | children | prompt                     |
      | ORCHESTRATOR | ORCHESTRATOR | COMPLETED | null     | WORK     | Build cloud infrastructure |
    And the expected events for this scenario are:
      | eventType           | nodeType     | targetNodeId | payloadFile                      |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | null         | orchestrator-status-changed.json |
      | NODE_STATUS_CHANGED | WORK         | null         | work-a-status-changed.json       |
      | NODE_STATUS_CHANGED | SUMMARY      | null         | summary-status-changed.json      |
      | GOAL_COMPLETED      | ORCHESTRATOR | null         | goal-completed.json              |
    And the mock response file ""
    When the graph execution completes and persistence is triggered
    Then the expected events should have been received
    And the OrchestratorNode should be stored in the database with status COMPLETED
    And all WorkNodes should be persisted with their final status
    And the SummaryNode should be persisted with COMPLETED status
    And all outputs should be queryable from the database

  @completion @multi_goal
  Scenario: Multiple concurrent goals complete independently
    Given multiple computation graphs with the following structure:
      | graphId | nodeId       | nodeType     | status | parentId | children | prompt                      |
      | goal-A  | ORCHESTRATOR | ORCHESTRATOR | READY  | null     | WORK     | Build search service        |
      | goal-B  | orch-B       | ORCHESTRATOR | READY  | null     | WORK1    | Build recommendation engine |
    And the expected events for this scenario are:
      | eventType           | nodeType     | targetNodeId | payloadFile                        |
      | NODE_STATUS_CHANGED | WORK         | null         | work-a1-status-changed.json        |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | null         | orchestrator-a-status-changed.json |
      | NODE_ADDED          | SUMMARY      | ORCHESTRATOR | summary-a-node-added.json          |
      | NODE_STATUS_CHANGED | SUMMARY      | null         | summary-a-status-completed.json    |
      | GOAL_COMPLETED      | ORCHESTRATOR | null         | goal-a-completed.json              |
    And the mock response file ""
    When goal-A completion check runs independently
    Then the expected events for goal-A should have been received
    And goal-B should continue executing without interruption
    And GoalCompletedEvent should be emitted only for goal-A
    And goal-B summary should be generated independently when it completes
