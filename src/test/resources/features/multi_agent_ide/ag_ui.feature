@multi_agent_ide @integration
Feature: Agent graph UI event consumption

  @ag_ui_live_graph @ag_ui_event_stream
  Scenario: UI renders live node creation and status events
    Given a computation graph with the following structure:
      | nodeId         | nodeType     | status | parentId | children | prompt                     |
      | orchestrator-1 | ORCHESTRATOR | READY  |          |          | Orchestrate goal execution |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile | order |
      | NODE_ADDED          | ORCHESTRATOR | none        |     0 |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | none        |     1 |
    When the graph execution completes
    Then the expected events should have been received

  @ag_ui_live_graph @ag_ui_event_stream
  Scenario: UI shows worktree metadata on creation
    Given a computation graph with the following structure:
      | nodeId      | nodeType | status | parentId | children | prompt           |
      | work-node-1 | WORK     | READY  |          |          | Build UI updates |
    And the expected events for this scenario are:
      | eventType        | nodeType | payloadFile | order |
      | WORKTREE_CREATED | WORKTREE | none        |     0 |
    When the graph execution completes
    Then the expected events should have been received

  @ag_ui_node_details @ag_ui_event_viewers
  Scenario: UI exposes node message and status history
    Given a computation graph with the following structure:
      | nodeId      | nodeType | status | parentId | children | prompt                 |
      | work-node-2 | WORK     | READY  |          |          | Provide status updates |
    And the expected events for this scenario are:
      | eventType           | nodeType | payloadFile | order |
      | ADD_MESSAGE_EVENT   | WORK     | none        |     0 |
      | NODE_STATUS_CHANGED | WORK     | none        |     1 |
    When the graph execution completes
    Then the expected events should have been received

  @ag_ui_agent_controls @ag_ui_user_controls
  Scenario: UI control actions emit events
    Given a computation graph with the following structure:
      | nodeId      | nodeType | status  | parentId | children | prompt           |
      | work-node-3 | WORK     | RUNNING |          |          | Await user input |
    And the expected events for this scenario are:
      | eventType   | nodeType | payloadFile | order |
      | PAUSE_EVENT | WORK     | none        |     0 |
      | STOP_AGENT  | WORK     | none        |     1 |
    When the graph execution completes
    Then the expected events should have been received

  @ag_ui_event_stream
  Scenario: UI keeps unsupported events visible
    Given a computation graph with the following structure:
      | nodeId         | nodeType     | status | parentId | children | prompt                   |
      | orchestrator-2 | ORCHESTRATOR | READY  |          |          | Observe unsupported event |
    And the expected events for this scenario are:
      | eventType         | nodeType     | payloadFile | order |
      | AVAILABLE_COMMANDS_UPDATE | ORCHESTRATOR | none        |     0 |
    When the graph execution completes
    Then the expected events should have been received

  @ag_ui_event_viewers
  Scenario: UI displays file write event details
    Given a computation graph with the following structure:
      | nodeId      | nodeType | status | parentId | children | prompt                |
      | work-node-4 | WORK     | READY  |          |          | Write file changes    |
    And the expected events for this scenario are:
      | eventType  | nodeType | payloadFile | order |
      | TOOL_CALL_RESULT | WORK     | none        |     0 |
    When the graph execution completes
    Then the expected events should have been received

  @ag_ui_event_viewers
  Scenario: UI aggregates streaming output events
    Given a computation graph with the following structure:
      | nodeId      | nodeType | status | parentId | children | prompt                |
      | work-node-5 | WORK     | READY  |          |          | Stream response output |
    And the expected events for this scenario are:
      | eventType         | nodeType | payloadFile | order |
      | NODE_STREAM_DELTA | WORK     | none        |     0 |
    When the graph execution completes
    Then the expected events should have been received
