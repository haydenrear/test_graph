@multi_agent_ide @ui @event_propagation
Feature: UI Event Propagation and Graph Visualization
  As a UI consumer
  I want to receive all graph changes as events
  So that the interface can be kept in sync with the backend computation graph

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events
    And a mock UI client is registered to receive events

  @ui @core
  Scenario: New node creation is propagated to UI
    When a goal is created and planned generating new WorkNodes
    Then NodeAddedEvent should be emitted for each new node
    And the mock UI should receive each NodeAddedEvent
    And the UI event should contain complete node data (id, type, status)
    And the UI should be able to render the new nodes

  @ui @status_updates
  Scenario: Node status changes are immediately reflected in UI
    Given a WorkNode in READY state
    When the node transitions to RUNNING
    Then NodeStatusChangedEvent should be emitted
    And the mock UI should receive the status change event
    And the UI should update the node's visual representation
    And the timestamp of the status change should be included

  @ui @streaming
  Scenario: Streaming tokens are sent to UI for incremental display
    Given a WorkNode in RUNNING state
    When the agent streams output tokens
    Then NodeStreamDeltaEvent should be emitted for each token
    And the mock UI should receive each streaming event
    And the UI should accumulate and display tokens incrementally
    And the UI should render the output in real-time

  @ui @graph_structure
  Scenario: UI can reconstruct complete graph from events
    Given a goal with planned nodes creating a complex structure
    And multiple nodes have been created and modified
    When the UI subscribes to all events
    Then the UI should receive all NodeAddedEvent and NodeUpdatedEvent
    And from these events, the UI should be able to reconstruct the full graph
    And parent-child relationships should be derivable from events
    And the UI should produce a valid visual graph representation

  @ui @branching_visualization
  Scenario: Branching operations are visualized correctly
    Given a WorkNode in execution
    When a branch is created with a modified goal
    Then NodeBranchedEvent should be emitted
    And the mock UI should receive the branching event
    And the UI should display both the original and branch nodes
    And the parent-child branch relationship should be clear
    And both nodes should be visually distinct in the graph

  @ui @annotation_display
  Scenario: Annotations are synchronized to UI
    Given a WorkNode with annotations added
    When an annotation event is emitted
    Then the mock UI should receive the annotation update
    And the UI should display the annotation on the node
    And multiple annotations on a single node should all be displayed
    And annotations should be associated with timestamps and authors

  @ui @hierarchy_display
  Scenario: UI displays correct hierarchical node structure
    Given a goal with OrchestratorNode and child WorkNodes
    And some WorkNodes have child ReviewNodes
    When the graph is rendered
    Then the OrchestratorNode should be at the top level
    And WorkNodes should be displayed as children of OrchestratorNode
    And ReviewNodes should be nested under their parent WorkNodes
    And indentation or visual hierarchy should reflect the structure

  @ui @error_notification
  Scenario: Error states are propagated to UI
    Given a WorkNode fails during execution
    When the node transitions to FAILED state
    Then NodeStatusChangedEvent should indicate FAILED status
    And the mock UI should display an error indicator
    And error details should be included in the event
    And the UI should highlight the failed node visually

  @ui @concurrent_updates
  Scenario: UI correctly handles concurrent updates from multiple nodes
    Given 3 nodes executing in parallel
    When multiple NodeStatusChangedEvent and NodeStreamDeltaEvent occur concurrently
    Then the mock UI should receive all events
    And the UI should correctly attribute updates to their respective nodes
    And concurrent updates should not cause UI inconsistencies
    And the UI should maintain a coherent view of the graph

  @ui @event_filtering
  Scenario: UI can subscribe to specific event types
    Given the mock UI is configured to receive only specific event types
    When both NodeStatusChangedEvent and NodeStreamDeltaEvent occur
    Then the UI should only receive the subscribed event types
    And non-subscribed events should be filtered out
    And the subscription should be effective and performant

  @ui @reconnection
  Scenario: UI can reconnect and recover state
    Given a UI is connected to the websocket and receiving events
    When the connection drops
    And the UI reconnects
    Then the UI should request the current graph state
    And the backend should send a state snapshot
    And any events missed during disconnection should be retrievable
    And the UI should synchronize to the current state

  @ui @export_graph
  Scenario: Complete graph state can be exported for UI display
    Given a goal execution has progressed with multiple nodes
    When a snapshot request is sent
    Then the backend should return the complete graph state
    And the state should include all nodes with current status
    And parent-child relationships should be included
    And the state should be JSON serializable for UI consumption

  @ui @performance
  Scenario: Event propagation maintains acceptable latency
    Given a WorkNode streaming output
    When NodeStreamDeltaEvent is emitted
    Then the mock UI should receive the event within 50ms
    And the UI should be able to display tokens without perceived lag
    And even with high token generation rate, latency should remain acceptable
    And the UI should not become unresponsive

  @ui @backward_compatibility
  Scenario Outline: Different UI versions can handle events
    Given a mock UI implementing event schema version "<version>"
    When various events are emitted
    Then the UI should correctly deserialize and handle events
    And the UI should not crash on unknown event fields
    And new fields should not break older UI versions
    And forward and backward compatibility should be maintained

    Examples:
      | version |
      | 1.0     |
      | 1.1     |
      | 2.0     |
