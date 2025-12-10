@multi_agent_ide @worktrees @branching
Feature: Worktree Branching and Parallel Exploration
  As a user and orchestrator
  I want to branch nodes and explore alternative solutions in parallel worktrees
  So that different approaches can be evaluated and merged later

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events
    And git is properly configured in the container

  @branching @core
  Scenario: Branching a node creates derived worktree
    Given a WorkNode in execution with worktree on branch "work-original"
    When a branch request is sent with modified goal "Alternative approach: use async pattern"
    Then a new WorkNode should be created
    And a new worktree should be created derived from parent state
    And the new worktree should reference the original's parentWorktreeId
    And NodeBranchedEvent should be emitted
    And WorktreeBranchedEvent should be emitted with parent/branch relationship

  @branching @independent_paths
  Scenario: Branches execute independently with separate results
    Given two branch WorkNodes from the same parent
    And branch A takes approach "Solution 1"
    And branch B takes approach "Solution 2"
    When both branches execute in parallel
    Then both should stream tokens independently
    And both should generate separate diffs in their worktrees
    And changes in branch A should not affect branch B
    And both NodeStreamDeltaEvent streams should be independent

  @branching @planning_spec_branch
  Scenario: Planning specs are created for branch worktrees
    Given a branch WorkNode creates its worktree
    When the branch worktree is initialized
    Then a planning spec file should be created at {branchWorktreePath}/.multi-agent-plan
    And the parentWorktreeId in the spec should reference the original parent
    And the spec should note that this is a branch with modified goal
    And the spec should be committed to the branch's git history

  @branching @shared_ancestry
  Scenario: Branch worktrees share common ancestor with original
    Given a parent WorkNode with worktree at commit ABC123
    And a branch WorkNode created at that point
    When the branch worktree is created
    Then the branch should be created at commit ABC123
    And the original and branch share commit ABC123 as common ancestor
    And both can independently advance from this point
    And merging can use three-way merge based on common ancestor

  @branching @multiple_levels
  Scenario: Branches can create sub-branches recursively
    Given a WorkNode A with worktree
    And a branch B from A
    And a branch B1 from B
    When all are initialized
    Then the hierarchy should be: A -> B -> B1
    And each should have correct parentWorktreeId references
    And planning specs should correctly reflect the lineage
    And git history should preserve all branch points

  @branching @merge_branches
  Scenario: Branches can be merged back to original
    Given two branches A1 and A2 from original node A
    And both A1 and A2 have completed work
    When merge requests are sent for both
    Then both should merge back into A's worktree
    And A's worktree should contain changes from both branches
    And the order of merges may matter (merge A1 first, then A2)
    And WorktreeMergedEvent should be emitted for each merge

  @branching @parallel_branch_merge
  Scenario: Results from parallel branches are aggregated
    Given 3 branches exploring different solutions
    And all 3 have completed execution
    When a merge/aggregation is requested
    Then a new aggregation WorkNode should be created
    And all 3 branch results should be available for the aggregator
    And the aggregator can decide which approach to use or combine them
    And the final result should be merged into the original parent

  @branching @branch_comparison
  Scenario: Branches can be compared before merge decision
    Given two branch worktrees with different solutions
    When a comparison is requested
    Then diffs from both branches should be displayed side-by-side
    And the NodeUpdatedEvent should contain both diffs
    And token counts, code metrics, or other quality indicators should be compared
    And the user can review both approaches before deciding

  @branching @branch_abandonment
  Scenario: Unsuccessful branches can be abandoned
    Given a branch WorkNode that failed or produced poor results
    When an abandonment message is sent
    Then the branch should transition to PRUNED or ABANDONED state
    And the branch worktree may be marked for cleanup
    And NodePrunedEvent should be emitted
    And the branch worktree planning spec should record abandonment

  @branching @branch_git_history
  Scenario: Branch creation is recorded in git history
    Given a parent worktree and branch worktree
    When examining the parent's git log
    Then the branch point should not be visible in parent's log (separate branch)
    And the branch worktree's git log should show its own history
    And upon merge, the merge commit should appear in parent log
    And the complete branch history should be reconstructible

  @branching @branch_rebase_option
  Scenario: Branches can be rebased onto updated parent
    Given a parent worktree at commit A
    And a branch created at A
    And parent has advanced to commit B
    And branch has work to merge
    When a rebase-then-merge is requested
    Then the branch should be rebased onto commit B
    And the branch commits should be replayed on top of B
    And conflicts may occur if both changed same files
    And the rebased branch should then be mergeable cleanly

  @branching @branch_isolation
  Scenario: Branch changes are isolated until explicitly merged
    Given a parent worktree and a branch worktree
    When the branch makes changes
    Then the parent worktree should not see any changes
    And the parent should be able to continue working independently
    And isolation should be enforced by git (separate branches/worktrees)
    And only explicit merge operations should propagate changes

  @branching @planning_decision
  Scenario: Branching decisions can be made at planning time
    Given a planning node that creates subtasks
    And some subtasks should explore alternatives
    When planning is configured to branch certain nodes
    Then those nodes should be created as branches from the start
    And parallel exploration should be built into the plan
    And the planning spec should record branch intentions
    And execution should automatically parallelize the branches
