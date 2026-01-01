@multi_agent_ide @integration
Feature: Multi-agent IDE end-to-end orchestration coverage

  Background:
    Given the test configuration is:
      | key               | value  |
      | MODEL_TYPE        | openai |
      | SPRING_PROFILES   | openai |
      | SUBSCRIPTION_TYPE | sse    |

  @multi_agent_ide_end_to_end_no_submodules
  Scenario: End-to-end orchestration completes without submodules
    Given a computation graph with the following structure:
      | nodeId         | nodeType     | status | parentId | children | prompt                     |
      | orchestrator-1 | ORCHESTRATOR | READY  |          |          | Orchestrate goal execution |
    And the mock response file "multi_agent_ide/end_to_end_no_submodules.json"
    And the expected events for this scenario are:
      | eventType             | nodeType     | payloadFile | order |
      | NODE_ADDED            | ORCHESTRATOR | none        | 0     |
      | NODE_STATUS_CHANGED   | ORCHESTRATOR | none        | 1     |
      | NODE_STATUS_CHANGED   | WORK         | none        | 2     |
      | NODE_STATUS_CHANGED   | PLANNING     | none        | 3     |
      | NODE_REVIEW_REQUESTED | AGENT_REVIEW | none        | 4     |
    When the graph execution completes
    Then the expected events should have been received
    And no additional events should have been captured

  @multi_agent_ide_end_to_end_with_submodules
  Scenario: End-to-end orchestration completes with submodules
    Given a computation graph with the following structure:
      | nodeId         | nodeType     | status | parentId | children | prompt                     |
      | orchestrator-2 | ORCHESTRATOR | READY  |          |          | Orchestrate goal execution |
    And the mock response file "multi_agent_ide/end_to_end_with_submodules.json"
    And the expected events for this scenario are:
      | eventType           | nodeType | payloadFile | order |
      | WORKTREE_CREATED    | WORKTREE | none        | 0     |
      | WORKTREE_CREATED    | WORKTREE | none        | 1     |
      | NODE_STATUS_CHANGED | WORK     | none        | 2     |
      | NODE_STATUS_CHANGED | PLANNING | none        | 3     |
    When the graph execution completes
    Then the expected events should have been received
    And no additional events should have been captured

  @multi_agent_ide_revision_failure
  Scenario: Revision cycle and failure handling are visible in the event stream
    Given a computation graph with the following structure:
      | nodeId         | nodeType     | status | parentId | children | prompt                  |
      | orchestrator-3 | ORCHESTRATOR | READY  |          |          | Execute ticket workflow |
    And the mock response file "multi_agent_ide/revision_failure_cycle.json"
    And the expected events for this scenario are:
      | eventType             | nodeType     | payloadFile | order |
      | NODE_REVIEW_REQUESTED | AGENT_REVIEW | none        | 0     |
      | NODE_STATUS_CHANGED   | AGENT_REVIEW | none        | 1     |
      | NODE_STATUS_CHANGED   | WORK         | none        | 2     |
    When the graph execution completes
    Then the expected events should have been received
    And no additional events should have been captured
