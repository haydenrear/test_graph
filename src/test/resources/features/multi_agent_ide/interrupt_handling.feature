@multi_agent_ide @integration
Feature: Interrupt handling and continuation routing

  Background:
    Given the test configuration is:
      | key               | value                 |
      | MODEL_TYPE        | openai                |
      | SPRING_PROFILES   | openai                |
      | SUBSCRIPTION_TYPE | sse                   |
      | BASE_URL          | http://localhost:8080 |

  @interrupt_review_resume
  Scenario: Review interrupt pauses and returns to the originating work
    Given a computation graph with the following structure:
      | nodeId           | nodeType     | status | parentId | children | prompt                            |
      | interrupt-orch-1 | ORCHESTRATOR | READY  |          |          | Orchestrate review interrupt flow |
    And the mock response file "multi_agent_ide/interrupt_handling.json"
    And the expected events for this scenario are:
      | eventType             | nodeType     | payloadFile | order |
      | NODE_REVIEW_REQUESTED | WORK         | none        |     0 |
      | NODE_ADDED            | HUMAN_REVIEW | none        |     1 |
      | NODE_STATUS_CHANGED   | WORK         | none        |     2 |
    When the graph execution completes
    Then the expected events should have been received
    And no additional events should have been captured

  @interrupt_pause_resume
  Scenario: Pause interrupt waits for input and resumes the same stage
    Given a computation graph with the following structure:
      | nodeId           | nodeType     | status | parentId | children | prompt                           |
      | interrupt-orch-2 | ORCHESTRATOR | READY  |          |          | Orchestrate pause interrupt flow |
    And the mock response file "multi_agent_ide/interrupt_handling.json"
    And the expected events for this scenario are:
      | eventType           | nodeType  | payloadFile | order |
      | PAUSE_EVENT         | WORK      | none        |     0 |
      | NODE_STATUS_CHANGED | WORK      | none        |     1 |
      | NODE_ADDED          | INTERRUPT | none        |     2 |
    When the graph execution completes
    Then the expected events should have been received
    And no additional events should have been captured

  @interrupt_route_control
  Scenario: Stop or prune interrupt prevents downstream continuation
    Given a computation graph with the following structure:
      | nodeId           | nodeType     | status | parentId | children | prompt                      |
      | interrupt-orch-3 | ORCHESTRATOR | READY  |          |          | Orchestrate stop/prune flow |
    And the mock response file "multi_agent_ide/interrupt_handling.json"
    And the expected events for this scenario are:
      | eventType           | nodeType  | payloadFile | order |
      | STOP_AGENT          | WORK      | none        |     0 |
      | NODE_STATUS_CHANGED | WORK      | none        |     1 |
      | NODE_ADDED          | INTERRUPT | none        |     2 |
    When the graph execution completes
    Then the expected events should have been received
    And no additional events should have been captured
