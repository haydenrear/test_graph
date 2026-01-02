@multi_agent_ide @integration
Feature: Agent graph UI event consumption

  Background:
    Given the test configuration is:
      | key               | value                                   |
      | MODEL_TYPE        | openai                                  |
      | SPRING_PROFILES   | openai                                  |
      | SUBSCRIPTION_TYPE | selenium                                |
      | BASE_URL          | http://localhost:8080                   |
      | RECORD_VIDEO      | true                                    |
      | VIDEO_NAME        | ag_ui.feature.mp4                       |
      | VIDEO_OUTPUT_PATH | build/selenium/videos/ag_ui.feature.mp4 |
      | VIDEO_SCREEN_SIZE |                               1920x1080 |

  @ag_ui_live_graph @ag_ui_event_stream
  Scenario: UI renders live node creation and status events
    Given a computation graph with the following structure:
      | nodeId         | nodeType     | status | parentId | children | prompt                     |
      | orchestrator-1 | ORCHESTRATOR | READY  |          |          | Orchestrate goal execution |
    And the mock response file "multi_agent_ide/ag_ui.json"
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
    And the mock response file "multi_agent_ide/ag_ui.json"
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
    And the mock response file "multi_agent_ide/ag_ui.json"
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
    And the mock response file "multi_agent_ide/ag_ui.json"
    And the expected events for this scenario are:
      | eventType   | nodeType | payloadFile | order |
      | PAUSE_EVENT | WORK     | none        |     0 |
      | STOP_AGENT  | WORK     | none        |     1 |
    When the graph execution completes
    Then the expected events should have been received

  @ag_ui_event_stream
  Scenario: UI keeps unsupported events visible
    Given a computation graph with the following structure:
      | nodeId         | nodeType     | status | parentId | children | prompt                    |
      | orchestrator-2 | ORCHESTRATOR | READY  |          |          | Observe unsupported event |
    And the mock response file "multi_agent_ide/ag_ui.json"
    And the expected events for this scenario are:
      | eventType                 | nodeType     | payloadFile | order |
      | AVAILABLE_COMMANDS_UPDATE | ORCHESTRATOR | none        |     0 |
    When the graph execution completes
    Then the expected events should have been received

  @ag_ui_event_viewers
  Scenario: UI displays file write event details
    Given a computation graph with the following structure:
      | nodeId      | nodeType | status | parentId | children | prompt             |
      | work-node-4 | WORK     | READY  |          |          | Write file changes |
    And the mock response file "multi_agent_ide/ag_ui.json"
    And the expected events for this scenario are:
      | eventType        | nodeType | payloadFile | order |
      | TOOL_CALL_RESULT | WORK     | none        |     0 |
    When the graph execution completes
    Then the expected events should have been received

  @ag_ui_event_viewers
  Scenario: UI aggregates streaming output events
    Given a computation graph with the following structure:
      | nodeId      | nodeType | status | parentId | children | prompt                 |
      | work-node-5 | WORK     | READY  |          |          | Stream response output |
    And the mock response file "multi_agent_ide/ag_ui.json"
    And the expected events for this scenario are:
      | eventType         | nodeType | payloadFile | order |
      | NODE_STREAM_DELTA | WORK     | none        |     0 |
    When the graph execution completes
    Then the expected events should have been received

  @ag_ui_selenium_e2e
  Scenario: UI test probe captures orchestrator lifecycle events
    Given the mock response file "multi_agent_ide/ag_ui.json"
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile | order |
      | NODE_ADDED          | ORCHESTRATOR | none        |     0 |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | none        |     1 |
    When the graph execution completes
    Then the expected events should have been received
