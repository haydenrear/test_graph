@multi_agent_ide @branching @editing @interruption
Feature: Branching, Editing, and Interruption Operations
  As a user
  I want to branch, edit, and interrupt nodes during execution
  So that I can explore alternatives and guide the computation graph dynamically

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events

  @branching @core
  Scenario: User branches a node with modified goal
    Given a WorkNode exists with goal "Implement user authentication"
    When a branch request is sent via message with modified goal "Implement authentication with OAuth2"
    Then a new WorkNode should be created as a branch
    And the branch node should have status READY
    And NodeBranchedEvent should be emitted
    And the parent node relationship should be recorded
    And the original node should remain in its current state

  @branching @parallel_exploration
  Scenario: Multiple branches create parallel exploration paths
    Given a WorkNode with description "Design API structure"
    When two branch requests are sent with different goals
    Then two new WorkNodes should be created as siblings
    And both should have status READY
    And both should be independent and executable
    And results from both branches should be tracked separately

  @branching @merge
  Scenario: Branches can merge their results back to parent
    Given two branch WorkNodes have completed execution
    And both are in COMPLETED state with different results
    When a merge request is sent via message
    Then a new OrchestratorNode should be created to merge results
    And the merge node should aggregate both branch outputs
    And a NodeAddedEvent should be emitted for the merge node
    And the graph should reflect the merged structure

  @editing @core
  Scenario: User edits a node prompt before execution
    Given a WorkNode in READY state with original prompt "Write a function"
    When an edit message is sent with new prompt "Write an async function with error handling"
    Then the node prompt should be updated
    And a NodeUpdatedEvent should be emitted
    And the node should remain READY for execution
    And the edit history should be preserved

  @editing @during_execution
  Scenario: Editing a RUNNING node pauses and resumes execution
    Given a WorkNode in RUNNING state with partial streaming output
    When an edit message is sent to interrupt and update the prompt
    Then the node should transition to WAITING_INPUT
    And NodeStatusChangedEvent should be emitted
    And the partial output should be preserved
    And upon resumption message, execution should continue with new prompt

  @editing @validation
  Scenario: Edited prompts are validated before resumption
    Given a node in WAITING_INPUT state with edited prompt
    When validation is triggered
    Then the prompt should be checked for validity
    And if valid, the node should transition back to READY
    And if invalid, an error should be returned via message
    And the user should be notified of validation results

  @interruption @core
  Scenario: User interrupts a RUNNING node
    Given a WorkNode in RUNNING state actively streaming
    And multiple tokens have been streamed
    When an interrupt message is sent
    Then the execution should stop immediately
    And the node should transition to WAITING_INPUT state
    And NodeStatusChangedEvent should be emitted with WAITING_INPUT
    And partial output should be available for review

  @interruption @recovery
  Scenario: Interrupted node can be resumed or abandoned
    Given a WorkNode in WAITING_INPUT state from interruption
    And partial output is available
    When a resume message is sent
    Then the node should transition back to RUNNING
    And execution should continue with the remaining work
    And a NodeStatusChangedEvent should indicate resumption

  @interruption @abandon
  Scenario: Interrupted node can be abandoned and marked complete
    Given a WorkNode in WAITING_INPUT state from interruption
    When an abandon message is sent
    Then the node should transition to COMPLETED
    And partial output should be marked as incomplete
    And an annotation should record the abandonment reason
    And dependent nodes should not execute

  @interruption @cascade
  Scenario: Interrupting parent node affects child execution
    Given an OrchestratorNode with multiple child WorkNodes
    And some children are RUNNING
    When an interrupt message is sent to the parent
    Then all RUNNING child nodes should transition to WAITING_INPUT
    And all PENDING child nodes should transition to CANCELED
    And NodeStatusChangedEvent should be emitted for all affected nodes

  @pruning @core
  Scenario: User can prune a node and its subtree
    Given a WorkNode and its dependent children exist
    When a prune message is sent for the WorkNode
    Then the WorkNode should transition to PRUNED state
    And all descendant nodes should also transition to PRUNED
    And NodePrunedEvent should be emitted for the root pruned node
    And the pruned subtree should be removed from active execution

  @pruning @persistence
  Scenario: Pruned nodes are preserved in database for history
    Given nodes have been pruned via pruning operation
    When the graph is queried
    Then the pruned nodes should still exist in the database
    And they should be marked with PRUNED status
    And they should not be returned in active node queries
    And they should be retrievable in history queries

  @annotation @editorial
  Scenario: Users can annotate nodes with notes and context
    Given a WorkNode in any state
    When an annotation message is sent with content "Consider performance implications"
    Then the annotation should be attached to the node
    And an annotation event should be emitted
    And multiple annotations should be supported on a single node
    And annotations should be retrievable with the node

  @editing_history
  Scenario: Complete edit history is maintained
    Given a WorkNode is created with original prompt
    When it is edited multiple times with different prompts
    Then each edit should be recorded with timestamp and editor info
    And the edit history should be queryable
    And it should be possible to view the node at any point in history
    And diffs between versions should be computable

  @complex_operations
  Scenario: Complex workflow of branch, edit, interrupt, and resume
    Given a WorkNode in READY state
    When a branch is created with modified goal
    And the original is edited with new prompt
    And during execution it is interrupted
    And partial output is reviewed
    And execution is resumed
    Then the complete operation sequence should be recorded
    And the final state should be consistent
    And all intermediate states should be persisted
