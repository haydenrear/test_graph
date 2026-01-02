@multi_agent_ide @integration
Feature: Agent session isolation for multi-agent chats

  Background:
    Given the test configuration is:
      | key               | value                 |
      | MODEL_TYPE        | openai                |
      | SPRING_PROFILES   | openai                |
      | SUBSCRIPTION_TYPE | sse                   |
      | BASE_URL          | http://localhost:8080 |

  @chat_sessions_resume
  Scenario: Orchestrator resumes its prior context after sub-agent work
    Given a computation graph with the following structure:
      | nodeId         | nodeType     | status | parentId | children | prompt                        |
      | orchestrator-1 | ORCHESTRATOR | READY  |          |          | Orchestrate planning workflow |
    And the mock response file "multi_agent_ide/chat_sessions.json"
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile | order |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | none        |     0 |
      | NODE_STATUS_CHANGED | PLANNING     | none        |     1 |
    When the graph execution completes
    Then the expected events should have been received
    And no additional events should have been captured

  @chat_sessions_isolation
  Scenario: Multiple agents keep isolated sessions
    Given multiple computation graphs with the following structure:
      | graphId | nodeId         | nodeType     | status | parentId | prompt                       |
      | graph-A | orchestrator-2 | ORCHESTRATOR | READY  |          | Orchestrate goal execution A |
      | graph-B | orchestrator-3 | ORCHESTRATOR | READY  |          | Orchestrate goal execution B |
    And the mock response file "multi_agent_ide/chat_sessions.json"
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile | order |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | none        |     0 |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | none        |     1 |
    When the graph execution completes
    Then the expected events should have been received
    And no additional events should have been captured

  @chat_sessions_recovery
  Scenario: New agent starts with a fresh session
    Given a computation graph with the following structure:
      | nodeId         | nodeType     | status | parentId | children | prompt                      |
      | orchestrator-4 | ORCHESTRATOR | READY  |          |          | Orchestrate discovery setup |
    And the mock response file "multi_agent_ide/chat_sessions.json"
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile | order |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | none        |     0 |
    When the graph execution completes
    Then the expected events should have been received
    And no additional events should have been captured
