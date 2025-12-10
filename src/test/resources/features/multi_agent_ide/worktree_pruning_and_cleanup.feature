@multi_agent_ide @worktrees @pruning
Feature: Worktree Pruning and Cleanup
  As a user and orchestrator
  I want to prune unused branches and clean up worktrees
  So that the repository remains clean and abandoned work is archived

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events
    And git is properly configured in the container

  @pruning @core
  Scenario: Pruning a node removes its entire worktree subtree
    Given a WorkNode with associated worktree
    And the node has child nodes with their own worktrees
    When a prune message is sent for the parent node
    Then the parent WorkNode should transition to PRUNED state
    And all child WorkNodes should transition to PRUNED state
    And a NodePrunedEvent should be emitted for the parent
    And each child should emit its own NodePrunedEvent

  @pruning @worktree_cleanup
  Scenario: Pruned worktrees are marked as DISCARDED
    Given a WorkNode with worktree in ACTIVE status
    When the node is pruned
    Then the planning spec should be updated to mark status as DISCARDED
    And a WorktreeDiscardedEvent should be emitted
    And the worktree directory structure may be retained for history
    And the git branch associated with the worktree may be deleted or archived

  @pruning @recursive_cleanup
  Scenario: Pruning cascades through entire subtree
    Given a hierarchy: Node A (root) -> B -> C (3 levels deep)
    And all have active worktrees
    When pruning is requested for node A
    Then nodes A, B, and C should all transition to PRUNED
    And their worktrees should become DISCARDED
    And the cascade should happen atomically
    And all three WorktreeDiscardedEvent should be emitted

  @pruning @partial_subtree
  Scenario: Pruning one branch doesn't affect sibling branches
    Given a parent node with two branches: A and B
    And both A and B are ACTIVE with worktrees
    When only branch A is pruned
    Then node A should transition to PRUNED
    And node B should remain ACTIVE
    And B's worktree should remain unchanged
    And only NodePrunedEvent for A should be emitted

  @pruning @git_history_preservation
  Scenario: Pruned worktree history is preserved in git
    Given a worktree that has been pruned
    When the git log is examined
    Then all commits in the pruned worktree should still exist in git history
    And the commits should be on the worktree's branch
    And the branch may be deleted but commits remain in reflog
    And the pruned work can be recovered if needed via git reflog

  @pruning @archive_planning_spec
  Scenario: Planning specs for pruned nodes are archived
    Given a pruned WorkNode with planning spec
    When the pruned state is committed
    Then the planning spec should be updated with DISCARDED status and timestamp
    And the update should be committed to git with message "Archive planning spec for pruned node"
    And the historical planning spec remains in git history
    And future discovery scans should skip DISCARDED nodes by default

  @pruning @dependent_nodes
  Scenario: Pruning affects dependent downstream nodes
    Given a workflow: A -> B -> C (where B depends on A, C depends on B)
    And all are ACTIVE
    When node A is pruned
    Then A should become PRUNED
    And B should transition to FAILED or CANCELED (cannot proceed without A)
    And C should transition to CANCELED (cannot proceed without B)
    And appropriate event sequence should be emitted

  @pruning @user_confirmation
  Scenario: Pruning requires explicit user confirmation for safety
    Given a WorkNode that could be pruned
    When a prune message is sent
    Then the system should validate that pruning is intentional
    And optionally require a confirmation flag or message
    And only after confirmation, should NodePrunedEvent be emitted
    And if not confirmed, node should remain ACTIVE

  @pruning @recovery_potential
  Scenario: Pruned nodes can theoretically be recovered from git
    Given a pruned WorkNode with committed work
    When recovery is attempted
    Then the system should locate the worktree's git branch
    And checkout the commits associated with the pruned node
    And recreate the WorkNode if needed
    And this should be possible but not automatic (manual recovery)

  @pruning @cleanup_strategy
  Scenario Outline: Different cleanup strategies for pruned worktrees
    Given a pruned worktree
    When cleanup is triggered with strategy "<strategy>"
    Then the worktree should be handled according to "<action>"
    And the status should be recorded appropriately
    And the git history should be managed per strategy

    Examples:
      | strategy       | action                                           |
      | keep_history   | Delete directory, keep git commits and reflog    |
      | archive        | Move directory to archive location with metadata |
      | immediate      | Immediately delete directory and force-delete branch |
      | no_cleanup     | Leave worktree and branch intact, just mark pruned |

  @pruning @batch_pruning
  Scenario: Multiple nodes can be pruned in a batch operation
    Given a complex graph with multiple abandoned branches
    And a list of node IDs to prune
    When batch prune is triggered
    Then all listed nodes should transition to PRUNED
    And all associated worktrees should become DISCARDED
    And NodePrunedEvent should be emitted for each
    And the operation should be atomic or have clear ordering

  @pruning @planning_spec_update
  Scenario: Pruned worktree planning specs show final status
    Given a pruned worktree with planning spec
    When the planning spec is examined
    Then it should contain:
      | field              | example                |
      | nodeId             | work-node-uuid-123     |
      | status             | DISCARDED              |
      | pruningTimestamp   | 2025-12-10T15:45:00Z   |
      | pruningReason      | User requested pruning |
      | finalCommitHash    | abc123def456           |
    And this information should be helpful for understanding why pruning occurred

  @pruning @event_ordering
  Scenario: Pruning events are emitted in correct order
    When a complex subtree is pruned
    Then NodePrunedEvent should be emitted for all nodes in the subtree
    And WorktreeDiscardedEvent should be emitted for all worktrees
    And NodeStatusChangedEvent should be emitted to reflect PRUNED status
    And dependent nodes should receive FAILED or CANCELED events
    And the event sequence should be deterministic and recoverable
