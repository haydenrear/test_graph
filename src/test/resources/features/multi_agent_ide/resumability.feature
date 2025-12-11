@multi_agent_ide @resumability @state_recovery
Feature: Resumability and State Recovery
  As the orchestration system
  I want to recover system state from specs and resume execution after interruptions
  So that users can pause work and resume it later without losing progress

  Background:
    Given the multi-agent-ide test environment is initialized with:
      | parameter                   | value           |
      | dockerComposePath           | <composePath>   |
      | serviceToStart              | multi-agent-ide |
      | eventSubscriptionType       | websocket       |
      | testEventListenerSubscribed | all_events      |

  @resumability @spec_recovery
  Scenario: System recovers node state from specs on restart
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt            |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build new service |
    And the expected events for this scenario are:
    And the mock response file ""
    When the system restarts after shutdown
    Then the expected events should have been received
    And a WorkNode should be reconstructed from specs
    And the node status should be recovered as RUNNING
    And the node should resume execution from where it left off
    And no work should be lost

  @resumability @incomplete_work
  Scenario: Incomplete work is identified from spec Status sections
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt           |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build new module |
    And the expected events for this scenario are:
    And the mock response file ""
    When the system restarts and checks incomplete work
    Then the expected events should have been received
    And a WorkNode should be reconstructed from specs
    And the spec summary should be retrieved to understand overall state
    And the spec Status section should indicate incomplete work
    And the node should be queued for resumption
    And execution should continue from its last checkpoint

  @resumability @multiple_nodes
  Scenario: Multiple incomplete nodes are recovered and prioritized
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt              |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build entire system |
    And the expected events for this scenario are:
    And the mock response file ""
    When the system restarts with multiple incomplete nodes
    Then the expected events should have been received
    And a PlanningNode should be reconstructed as COMPLETED
    And work-A should be reconstructed as COMPLETED
    And work-B should be reconstructed as RUNNING (incomplete)
    And only work-B should be queued for resumption
    And execution order should respect dependencies

  @resumability @worktree_recovery
  Scenario: Worktree state is recovered from spec metadata
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt            |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build new service |
    And the expected events for this scenario are:
    And the mock response file ""
    When the system restarts with worktrees
    Then the expected events should have been received
    And a WorkNode should be reconstructed
    And the spec should contain worktree metadata
    And the worktree should be recovered to its previous state
    And uncommitted changes should be preserved
    And the worktree should be ready for resumption

  @resumability @submodule_state
  Scenario: Submodule worktree state is recovered including commit pointers
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                  |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build integrated system |
    And the expected events for this scenario are:
    And the mock response file ""
    When the system restarts with submodule worktrees
    Then the expected events should have been received
    And a WorkNode should be reconstructed with main and submodule worktrees
    And each submodule worktree should be recovered to its previous commit
    And submodule pointers should be restored correctly
    And the entire worktree hierarchy should be operational

  @resumability @spec_merge_state
  Scenario: Spec merge state is preserved for resuming merged work
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt           |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build new module |
    And the expected events for this scenario are:
    And the mock response file ""
    When the system restarts after spec merge
    Then the expected events should have been received
    And work-E should be reconstructed as COMPLETED
    And the orchestrator spec should reflect the merged state
    And the parent spec should show child work was completed
    And the system should understand the work was already merged

  @resumability @streaming_checkpoint
  Scenario: Streaming checkpoint allows resuming code generation
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt        |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Generate code |
    And the expected events for this scenario are:
    And the mock response file ""
    When the system restarts during code streaming
    Then the expected events should have been received
    And a WorkNode should be reconstructed in RUNNING state
    And the spec should contain the checkpoint of generated code
    And streaming should resume from the last checkpoint
    And tokens 3+ should be streamed to complete the generation
    And no duplicate tokens should be generated

  @resumability @parent_child_reconstruction
  Scenario: Parent and child relationships are reconstructed correctly
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt            |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build full system |
    And the expected events for this scenario are:
    And the mock response file ""
    When the system restarts with a hierarchy
    Then the expected events should have been received
    And a PlanningNode should be reconstructed as child of OrchestratorNode
    And a WorkNode should be reconstructed as child of PlanningNode
    And parent-child relationships should be restored
    And the entire hierarchy should be functional

  @resumability @idempotent
  Scenario: Resuming work is idempotent - running same work twice produces same results
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt        |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build service |
    And the expected events for this scenario are:
    And the mock response file ""
    When the system resumes work-G that was completed
    Then the expected events should have been received
    And work-G should be reconstructed as COMPLETED
    And the system should recognize the work was already done
    And re-running should not duplicate the work
    And the results should be consistent with original execution

  @resumability @error_recovery
  Scenario: Failed work can be identified and retried
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt       |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build system |
    And the expected events for this scenario are:
    And the mock response file ""
    When the system restarts with failed work
    Then the expected events should have been received
    And work-H should be reconstructed as FAILED
    And the failure reason should be included in spec
    And the system should queue the node for retry
    And users should be able to retry or edit the prompt before retry
    And the system should attempt execution again

  @resumability @branched_state
  Scenario: Branched nodes are recovered with all branches intact
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt          |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Explore options |
    And the expected events for this scenario are:
