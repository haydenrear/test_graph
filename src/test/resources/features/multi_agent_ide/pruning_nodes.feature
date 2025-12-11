@multi_agent_ide @pruning @cleanup
Feature: Pruning Nodes and Cleaning Up Worktrees
  As the orchestration system
  I want to prune nodes and clean up their associated worktrees and specs
  So that users can discard unwanted work and free up resources

  Background:
    Given the multi-agent-ide test environment is initialized with:
      | parameter                   | value           |
      | dockerComposePath           | <composePath>   |
      | serviceToStart              | multi-agent-ide |
      | eventSubscriptionType       | websocket       |
      | testEventListenerSubscribed | all_events      |

  @pruning @core
  Scenario: Node can be pruned to discard work
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt            |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build new service |
    And the expected events for this scenario are:
      | eventType           | nodeType | payloadFile                  |
      | NODE_ADDED          | WORK     | work-a-node-added.json       |
      | NODE_STATUS_CHANGED | WORK     | work-a-ready.json            |
      | NODE_PRUNED         | WORK     | work-a-pruned.json           |
      | WORKTREE_DISCARDED  | WORK     | work-a-worktree-deleted.json |
      | SPEC_DISCARDED      | WORK     | work-a-spec-deleted.json     |
    And the mock response file ""
    When the user prunes work-A
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And the node should transition to PRUNED status
    And the worktree should be marked as DISCARDED
    And the spec file should be deleted or archived
    And the node should be removed from the execution pipeline

  @pruning @main_and_submodules
  Scenario: Pruning discards all associated worktrees including submodules
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build payment service |
    And the expected events for this scenario are:
      | eventType          | nodeType | payloadFile                   |
      | NODE_ADDED         | WORK     | work-b-node-added.json        |
      | WORKTREE_CREATED   | WORK     | work-b-main-worktree.json     |
      | WORKTREE_CREATED   | WORK     | work-b-auth-submodule.json    |
      | WORKTREE_CREATED   | WORK     | work-b-payment-submodule.json |
      | NODE_PRUNED        | WORK     | work-b-pruned.json            |
      | WORKTREE_DISCARDED | WORK     | work-b-main-discarded.json    |
      | WORKTREE_DISCARDED | WORK     | work-b-auth-discarded.json    |
      | WORKTREE_DISCARDED | WORK     | work-b-payment-discarded.json |
    And the mock response file ""
    When the user prunes work-B with submodules
    Then the expected events should have been received
    And a WorkNode should be created with main and submodule worktrees
    And all worktrees should be marked as DISCARDED
    And the node should transition to PRUNED status
    And the spec should be deleted
    And all associated resources should be cleaned up

  @pruning @descendants
  Scenario: Pruning a node also prunes all descendants
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt            |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build new feature |
    And the expected events for this scenario are:
      | eventType          | nodeType | payloadFile                  |
      | NODE_ADDED         | PLANNING | planning-node-added.json     |
      | NODE_ADDED         | WORK     | work-a-node-added.json       |
      | NODE_ADDED         | WORK     | work-b-node-added.json       |
      | NODE_PRUNED        | PLANNING | planning-pruned.json         |
      | NODE_PRUNED        | WORK     | work-a-pruned.json           |
      | NODE_PRUNED        | WORK     | work-b-pruned.json           |
      | WORKTREE_DISCARDED | WORK     | work-a-worktree-deleted.json |
      | WORKTREE_DISCARDED | WORK     | work-b-worktree-deleted.json |
    And the mock response file ""
    When the user prunes plan-1
    Then the expected events should have been received
    And a PlanningNode should be created with WorkNode children
    And pruning the PlanningNode should prune work-A and work-B
    And all descendant worktrees should be discarded
    And all descendant specs should be deleted
    And the entire subtree should be removed from execution

  @pruning @siblings_unaffected
  Scenario: Pruning a node does not affect sibling nodes
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt             |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build new services |
    And the expected events for this scenario are:
      | eventType           | nodeType | payloadFile                  |
      | NODE_ADDED          | WORK     | work-a-node-added.json       |
      | NODE_ADDED          | WORK     | work-b-node-added.json       |
      | NODE_PRUNED         | WORK     | work-a-pruned.json           |
      | NODE_STATUS_CHANGED | WORK     | work-b-running.json          |
      | WORKTREE_DISCARDED  | WORK     | work-a-worktree-deleted.json |
    And the mock response file ""
    When the user prunes work-A
    Then the expected events should have been received
    And work-A and work-B should be created as siblings
    And pruning work-A should mark it as PRUNED
    And work-B should remain active and unaffected
    And work-B should continue executing or be available for execution
    And only work-A resources should be cleaned up

  @pruning @partial_tree
  Scenario: Pruning partial tree cleans up only that subtree
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt              |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build full platform |
    And the expected events for this scenario are:
      | eventType           | nodeType | payloadFile              |
      | NODE_ADDED          | WORK     | branch-1-node-added.json |
      | NODE_ADDED          | WORK     | work-a-node-added.json   |
      | NODE_ADDED          | WORK     | branch-2-node-added.json |
      | NODE_ADDED          | WORK     | work-b-node-added.json   |
      | NODE_PRUNED         | WORK     | branch-1-pruned.json     |
      | NODE_PRUNED         | WORK     | work-a-pruned.json       |
      | NODE_STATUS_CHANGED | WORK     | branch-2-running.json    |
      | NODE_STATUS_CHANGED | WORK     | work-b-running.json      |
    And the mock response file ""
    When the user prunes branch-1 but leaves branch-2
    Then the expected events should have been received
    And branch-1 and branch-2 should be created as siblings
    And work-A should be created as child of branch-1
    And work-B should be created as child of branch-2
    And pruning branch-1 should prune branch-1 and work-A
    And branch-2 and work-B should remain unaffected

  @pruning @resource_cleanup
  Scenario: Pruning releases all associated resources
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt           |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build test suite |
    And the expected events for this scenario are:
      | eventType          | nodeType | payloadFile                  |
      | NODE_ADDED         | WORK     | work-c-node-added.json       |
      | WORKTREE_CREATED   | WORK     | work-c-worktree.json         |
      | NODE_PRUNED        | WORK     | work-c-pruned.json           |
      | WORKTREE_DISCARDED | WORK     | work-c-worktree-deleted.json |
    And the mock response file ""
    When the user prunes work-C
    Then the expected events should have been received
    And a WorkNode should be created with its worktree
    And pruning should mark worktree as DISCARDED
    And the spec file should be deleted
    And the node should be removed from memory
    And all resources should be freed for reuse

  @pruning @status_transition
  Scenario: Pruned node status is recorded as PRUNED
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt       |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build module |
    And the expected events for this scenario are:
      | eventType           | nodeType | payloadFile            |
      | NODE_ADDED          | WORK     | work-d-node-added.json |
      | NODE_STATUS_CHANGED | WORK     | work-d-ready.json      |
      | NODE_PRUNED         | WORK     | work-d-pruned.json     |
    And the mock response file ""
    When the user prunes work-D
    Then the expected events should have been received
    And a WorkNode should be created
    And the node status should transition to PRUNED
    And the NODE_PRUNED event should be emitted
    And the status should be queryable as PRUNED

  @pruning @branched_nodes
  Scenario: Pruning one branch does not affect other branches
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Explore alternatives |
    And the expected events for this scenario are:
      | eventType           | nodeType | payloadFile               |
      | NODE_ADDED          | WORK     | work-e-node-added.json    |
      | NODE_BRANCHED       | WORK     | work-e-alt1-branched.json |
      | NODE_BRANCHED       | WORK     | work-e-alt2-branched.json |
      | NODE_PRUNED         | WORK     | work-e-alt1-pruned.json   |
      | NODE_STATUS_CHANGED | WORK     | work-e-alt2-running.json  |
      | WORKTREE_DISCARDED  | WORK     | work-e-alt1-deleted.json  |
    And the mock response file ""
    When the user prunes work-E-alt1 but keeps work-E-alt2
    Then the expected events should have been received
    And work-E should be created with branches work-E-alt1 and work-E-alt2
    And pruning work-E-alt1 should mark it as PRUNED
    And work-E-alt2 should remain active
    And work-E-alt1 resources should be freed
    And work-E-alt2 can continue executing independently

  @pruning @goal_completion
  Scenario: Pruned nodes do not prevent goal completion
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt        |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build product |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile                 |
      | NODE_ADDED          | WORK         | work-a-node-added.json      |
      | NODE_ADDED          | WORK         | work-b-node-added.json      |
      | NODE_PRUNED         | WORK         | work-a-pruned.json          |
      | NODE_STATUS_CHANGED | WORK         | work-b-completed.json       |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | orchestrator-completed.json |
      | GOAL_COMPLETED      | ORCHESTRATOR | goal-completed.json         |
    And the mock response file ""
    When work-A is pruned but work-B completes
    Then the expected events should have been received
    And work-A should be created as child of OrchestratorNode
    And work-B should be created as child of OrchestratorNode
    And pruning work-A should not block goal completion
    And work-B can be the only leaf node completing
    And the goal should complete with work-A marked PRUNED and work-B COMPLETED

  @pruning @confirmation
  Scenario: Pruning may require confirmation for large subtrees
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build complex system |
    And the expected events for this scenario are:
      | eventType                      | nodeType | payloadFile                  |
      | NODE_ADDED                     | PLANNING | planning-node-added.json     |
      | NODE_ADDED                     | WORK     | work-a-node-added.json       |
      | NODE_ADDED                     | WORK     | work-b-node-added.json       |
      | NODE_ADDED                     | WORK     | work-c-node-added.json       |
      | PRUNING_CONFIRMATION_REQUESTED | PLANNING | pruning-confirm-request.json |
    And the mock response file ""
    When the user attempts to prune a large subtree
    Then the expected events should have been received
    And a PlanningNode should be created with multiple WorkNode children
    And pruning should request confirmation before proceeding
    And the system should indicate how many nodes will be pruned
    And the user should be able to confirm or cancel the operation
