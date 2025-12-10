@multi_agent_ide @completion @summary
Feature: Goal Completion and Result Summarization
  As the orchestration system
  I want to aggregate results when all work is complete
  So that users receive a comprehensive summary of what was accomplished

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events

  @completion @core
  Scenario: Goal completes when all terminal nodes reach completion
    Given a goal "Build REST API" with planned WorkNodes A, B, and C
    And node A has COMPLETED
    And node B has COMPLETED
    And node C has COMPLETED
    When the completion check is triggered
    Then a GoalCompletedEvent should be emitted
    And the OrchestratorNode should transition to COMPLETED state
    And NodeStatusChangedEvent should indicate completion

  @completion @partial
  Scenario: Goal completion handles pruned nodes correctly
    Given a goal with WorkNodes A, B, C, and D planned
    And nodes A and B have COMPLETED
    And node C has been PRUNED
    And node D is in READY state
    When the completion check considers pruned nodes
    Then the system should wait for node D to complete
    And pruned nodes should not block completion
    And when D completes, GoalCompletedEvent should fire

  @completion @failure_handling
  Scenario: Goal handles nodes that fail gracefully
    Given a goal with WorkNodes A, B, and C
    And node A has COMPLETED
    And node B has transitioned to FAILED state
    When the completion check is triggered
    Then GoalCompletedEvent should still be emitted
    And the event should include information about the FAILED node
    And the overall goal status should be COMPLETED_WITH_FAILURES
    And the event should contain failure details

  @summary @core
  Scenario: Summary node is created when goal completes
    Given a goal has completed with multiple WorkNodes
    When GoalCompletedEvent is processed
    Then a SummaryNode should be created
    And the SummaryNode should be in RUNNING state
    And the SummaryGraphAgent should execute
    And the summary should aggregate all work results

  @summary @streaming
  Scenario: Summary generation streams output like work nodes
    Given a SummaryNode is executing
    And it is aggregating results from multiple completed nodes
    When the SummaryGraphAgent generates the summary
    Then NodeStreamDeltaEvent should be emitted for summary tokens
    And the test listener should receive streaming tokens
    And the summary should be accumulated as it streams

  @summary @structure
  Scenario: Summary contains structured information about completion
    Given a goal with 5 WorkNodes has completed
    And results from all nodes are available
    When the SummaryNode completes
    Then the summary should contain:
      | field              | description                          |
      | goal_description   | Original goal text                   |
      | nodes_executed     | Count of executed nodes              |
      | total_time         | Total execution time                 |
      | artifacts_created  | List of generated artifacts          |
      | issues_encountered | Any issues during execution          |
      | final_status       | COMPLETED or COMPLETED_WITH_FAILURES |

  @summary @annotations
  Scenario: Summary includes user annotations and notes
    Given a goal was executed with user annotations on various nodes
    And users added notes during execution
    When the summary is generated
    Then all annotations should be included in the summary
    And user notes should be preserved
    And the summary should reference where annotations were made

  @completion @event_ordering
  Scenario: Completion events are emitted in correct order
    When all terminal nodes of a goal are COMPLETED
    Then a NodeStatusChangedEvent should be emitted first
    And then a SummaryNode should be created with NodeAddedEvent
    And then the SummaryNode should begin execution
    And finally GoalCompletedEvent should be emitted last

  @persistence @database
  Scenario: Completed goal and summary are persisted
    Given a goal has completed with summary
    When the completion process finishes
    Then the OrchestratorNode should be stored as COMPLETED
    And all WorkNodes should be persisted with their final status
    And the SummaryNode should be persisted
    And all outputs should be queryable from the database

  @completion @metrics
  Scenario: Completion metrics are calculated and stored
    Given a goal execution completes
    When metrics are calculated
    Then the following should be recorded:
      | metric              | example              |
      | execution_duration  | 45000 milliseconds   |
      | nodes_executed      | 5                    |
      | nodes_failed        | 0                    |
      | human_review_count  | 2                    |
      | tokens_generated    | 12500                |
      | branches_created    | 1                    |

  @summary @export
  Scenario: Completed goal can be exported with full history
    Given a completed goal in the database
    When an export request is sent
    Then the export should include:
      | item                    |
      | OrchestratorNode        |
      | All WorkNodes and state |
      | Summary node            |
      | All events emitted      |
      | User annotations        |
      | Execution timeline      |

  @completion @multi_goal
  Scenario: Multiple concurrent goals complete independently
    Given goal A and goal B are executing in parallel
    And goal A completes all its nodes
    And goal B still has nodes executing
    When goal A completion check runs
    Then GoalCompletedEvent should be emitted only for goal A
    And goal B should continue executing unaffected
    And both summaries should be generated independently

  @completion @error_recovery
  Scenario: System recovers if completion check fails
    Given a goal is nearing completion
    And the completion check triggers an error
    When the error is caught and logged
    Then the system should retry the completion check
    And eventually succeed when ready
    And the retry should not create duplicate events
    And users should be notified of the transient error
