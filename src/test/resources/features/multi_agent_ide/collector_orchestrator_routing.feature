@multi_agent_ide @integration
Feature: Collector routing and review gating

  Background:
    Given the test configuration is:
      | key               | value  |
      | MODEL_TYPE        | openai |
      | SPRING_PROFILES   | openai |
      | SUBSCRIPTION_TYPE | sse    |

  @collector_orchestrator_reentry
  Scenario: Collector results trigger another planning pass
    Given a computation graph with the following structure:
      | nodeId                 | nodeType     | status | parentId | children | prompt                        |
      | orchestrator-routing-1 | ORCHESTRATOR | READY  |          |          | Orchestrate planning workflow |
    And the mock response file "multi_agent_ide/collector_orchestrator_routing.json"
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile | order |
      | NODE_STATUS_CHANGED | PLANNING     | none        | 0     |
      | NODE_ADDED          | PLANNING     | none        | 1     |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | none        | 2     |
    When the graph execution completes
    Then the expected events should have been received
    And no additional events should have been captured

  @collector_human_review_gate
  Scenario: Review gate pauses and directs the next phase
    Given a computation graph with the following structure:
      | nodeId                 | nodeType     | status | parentId | children | prompt                        |
      | orchestrator-routing-2 | ORCHESTRATOR | READY  |          |          | Orchestrate planning workflow |
    And the mock response file "multi_agent_ide/collector_orchestrator_routing.json"
    And the expected events for this scenario are:
      | eventType             | nodeType | payloadFile | order |
      | NODE_REVIEW_REQUESTED | PLANNING | none        | 0     |
      | NODE_STATUS_CHANGED   | PLANNING | none        | 1     |
    When the graph execution completes
    Then the expected events should have been received
    And no additional events should have been captured
