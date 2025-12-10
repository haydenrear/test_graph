@multi_agent_ide @work_execution @streaming
Feature: Work Execution with Streaming Output
  As an execution engine
  I want to stream work output incrementally to the UI
  So that users see progress in real-time as agents process subtasks

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events
    And LangChain4j streaming models are configured

  @work_execution @streaming @core
  Scenario: WorkNode transitions through execution states with streaming
    Given a goal "Refactor authentication module" is created and planned
    And a WorkNode with description "Extract auth logic into separate service" exists in READY state
    When the execution engine processes the READY node
    Then the node should transition to RUNNING
    And NodeStatusChangedEvent should be emitted with status "RUNNING"
    And the EditorGraphAgent should begin execution

  @work_execution @streaming @core
  Scenario: Streaming tokens are emitted as NodeStreamDeltaEvent
    Given a WorkNode is in RUNNING state
    And the EditorGraphAgent is executing with LangChain4j streaming
    When the agent generates response tokens
    Then NodeStreamDeltaEvent should be emitted for each token
    And the test listener should receive tokens incrementally
    And each event should contain the node ID and token content

  @work_execution @streaming
  Scenario: Streaming output is accumulated and stored
    Given a WorkNode is processing "Generate unit tests for authentication"
    And multiple streaming tokens are being emitted
    When the agent finishes execution
    Then all tokens should be accumulated into complete output
    And the WorkNode should transition to WAITING_REVIEW state
    And a NodeStatusChangedEvent should be emitted with status "WAITING_REVIEW"
    And the complete output should be stored with the node

  @work_execution @streaming
  Scenario Outline: Different agent types produce different streaming patterns
    Given a WorkNode with description "<description>" in RUNNING state
    And the executing agent type is "<agentType>"
    When the agent streams output
    Then the streaming pattern should match "<pattern>"
    And the final output should be properly formatted
    And the node should transition correctly to next state

    Examples:
      | description                   | agentType         | pattern              |
      | Write Python async code       | CodeGenAgent      | code_with_comments   |
      | Review code for security      | ReviewAgent       | structured_analysis  |
      | Generate documentation       | DocumentationAgent | markdown_formatted   |

  @work_execution @state_transitions
  Scenario: Work node completes after streaming and review
    Given a WorkNode has finished streaming output
    And a ReviewNode has approved the work
    When the node transitions from WAITING_REVIEW to COMPLETED
    Then NodeStatusChangedEvent should be emitted with status "COMPLETED"
    And GoalCompletedEvent should eventually be published if this is final node
    And the node result should be available for dependent nodes

  @work_execution @error_handling
  Scenario: Streaming handles interruption gracefully
    Given a WorkNode is RUNNING and streaming output
    And multiple tokens have been streamed
    When an interrupt signal is sent via message
    Then the streaming should stop
    And the node should transition to WAITING_INPUT state
    And partial output should be preserved
    And NodeStatusChangedEvent should indicate WAITING_INPUT

  @work_execution @parallelism
  Scenario: Multiple work nodes execute concurrently with streaming
    Given a goal is planned creating 3 independent WorkNodes
    And all nodes are in READY state
    When the execution engine steps through all nodes
    Then all 3 nodes should transition to RUNNING
    And each should emit its own NodeStatusChangedEvent
    And streaming events from all 3 should be received concurrently
    And the test listener should correctly attribute tokens to each node

  @work_execution @streaming
  Scenario: Streaming output contains context window information
    Given a WorkNode is executing with streaming
    When tokens are emitted via NodeStreamDeltaEvent
    Then each event should contain the node ID
    And the event should contain a sequence number
    And the complete flag should indicate if this is the final token
    And metadata should be included for UI rendering hints

  @work_execution @performance
  Scenario: Streaming maintains reasonable latency
    Given a WorkNode is streaming a large response
    When tokens are being emitted
    Then the time between successive NodeStreamDeltaEvent should be under 500ms
    And the test listener should not experience event queue overflow
    And memory usage should remain stable during streaming

  @work_execution @persistence
  Scenario: Streamed output is persisted to graph database
    Given a WorkNode has completed streaming
    And all tokens have been accumulated
    When the node is persisted
    Then the complete accumulated output should be stored in the database
    And the output should be queryable by node ID
    And the persisted output should match the streamed content exactly
