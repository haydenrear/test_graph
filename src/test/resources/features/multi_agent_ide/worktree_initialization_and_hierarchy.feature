@multi_agent_ide @worktrees @initialization
Feature: Worktree Initialization and Recursive Hierarchy Management
  As the execution engine
  I want to create and manage recursive git worktrees parallel to the computation graph
  So that each WorkNode has an isolated git workspace for safe code generation and editing

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events
    And a git repository is initialized at "<repoPath>"

  @worktrees @core
  Scenario: WorkNode creation triggers worktree initialization
    Given a goal "Refactor authentication" is created
    And a WorkNode is planned with description "Extract auth service"
    And the WorkNode is in READY state
    When the WorkNode is about to begin execution
    Then a WorktreeContext should be created
    And the worktree path should follow pattern "{repoRoot}/.worktrees/{worktreeId}"
    And a git worktree should be created at that path
    And a WorktreeCreatedEvent should be emitted
    And the WorkNode should be updated with worktree reference via NodeUpdatedEvent

  @worktrees @hierarchy
  Scenario: Recursive worktrees mirror computation graph hierarchy
    Given a goal with OrchestratorNode
    And planning creates 2 child WorkNodes: A and B
    And node A spawns 2 child WorkNodes: A1 and A2
    When all worktrees are initialized
    Then the directory structure should be:
      | path                                          |
      | {repoRoot}/.worktrees/{orchestrator_id}      |
      | {repoRoot}/.worktrees/{A_id}                 |
      | {repoRoot}/.worktrees/{B_id}                 |
      | {repoRoot}/.worktrees/{A1_id}                |
      | {repoRoot}/.worktrees/{A2_id}                |
    And A1 worktree parentWorktreeId should reference A's worktreeId
    And A2 worktree parentWorktreeId should reference A's worktreeId
    And B worktree parentWorktreeId should reference orchestrator's worktreeId

  @worktrees @parent_reference
  Scenario: Child worktrees maintain correct parent references
    Given a parent WorkNode with worktreeId "parent-123"
    When a child WorkNode is created
    Then the child's WorktreeContext.parentWorktreeId should equal "parent-123"
    And the child worktree should be created as a git branch from parent's current state
    And the parent worktree should not be modified by child creation
    And a WorktreeCreatedEvent should include the parentWorktreeId

  @worktrees @isolation
  Scenario: Worktrees provide isolated execution spaces
    Given two sibling WorkNodes A and B from same parent
    When WorkNode A modifies files in its worktree
    And WorkNode B modifies the same files in its worktree
    Then the changes in A's worktree should not affect B's worktree
    And both worktrees should have separate git index and working directory
    And changes can be merged later without automatic conflict

  @worktrees @base_branch
  Scenario: Worktrees are created with correct base branch
    Given a parent WorkNode with base branch "main"
    When a child WorkNode creates its worktree
    Then the child worktree should be branched from "main"
    And the WorktreeContext should store baseBranch = "main"
    And subsequent descendants should inherit "main" as base

  @worktrees @status_tracking
  Scenario: Worktree status transitions are tracked
    Given a WorktreeContext in ACTIVE state
    When the associated WorkNode is completed
    And the worktree is merged into parent
    Then the WorktreeContext.status should transition to MERGED
    And a WorktreeMergedEvent should be emitted
    And the worktree path remains on disk for history (optional cleanup)

  @worktrees @concurrent_creation
  Scenario: Multiple worktrees can be created concurrently
    Given a parent WorkNode with 5 planned child WorkNodes
    When all 5 children transition to READY simultaneously
    Then all 5 worktrees should be created in parallel
    And each should have a unique worktreeId
    And each should reference the parent's worktreeId
    And all WorktreeCreatedEvent should be emitted
    And no race conditions should corrupt any worktree

  @worktrees @event_sequence
  Scenario: Worktree initialization emits events in correct order
    When a WorkNode transitions from READY to RUNNING and initializes worktree
    Then a WorktreeCreatedEvent should be emitted first
    And then a NodeUpdatedEvent should link the node to the worktree
    And then NodeStatusChangedEvent should indicate RUNNING
    And the event sequence should be deterministic

  @worktrees @cleanup
  Scenario: Worktree cleanup on pruning
    Given a WorkNode with an active worktree in ACTIVE state
    When the node is pruned via pruning operation
    Then the WorktreeContext.status should become DISCARDED
    And a WorktreeDiscardedEvent should be emitted
    And optionally the worktree directory may be cleaned up
    And references to the worktree should be archived/retained for audit

  @worktrees @large_hierarchy
  Scenario: System handles deep worktree hierarchies
    Given a deeply nested graph 5 levels deep
    When all levels initialize their worktrees
    Then worktrees at level 5 should correctly reference level 4 parents
    And path depth should be manageable (no OS path length issues)
    And all parent references should form a valid tree structure
    And no circular references should exist

  @worktrees @planning_spec @resumability @core
  Scenario: Planning spec is committed to worktree root for resumability
    Given a WorkNode creates a new worktree
    When the worktree is initialized
    Then a spec file should be created at {worktreePath}/.multi-agent-plan.md
    And the spec should contain standard sections:
      | section      | content                                 |
      | Header       | Metadata: nodeId, timestamps, etc.      |
      | Plan         | Steps for this worktree's task          |
      | Status       | Initial status: 0% complete             |
      | Submodules   | (if repo has submodules)                |
    And the spec should include structured metadata:
      | field              | example                |
      | nodeId             | work-node-uuid-123     |
      | parentWorktreeId   | parent-uuid-456        |
      | baseBranch         | main                   |
      | nodePrompt         | Original task prompt   |
      | createdTimestamp   | 2025-12-10T10:30:00Z   |
    And the spec should be committed to git with message "Initialize spec for {nodeId}"
    And the commit should be on the working branch (e.g., "work-{nodeId}")
    And the spec should be executable (validated and queryable via tools)

  @worktrees @discovery
  Scenario: Worktrees are discoverable from git repository structure
    Given multiple nested worktrees have been created
    When a discovery scan is run on the repository root
    Then the system should walk through {repoRoot}/.worktrees directory
    And for each directory, read the .multi-agent-plan file
    And reconstruct the complete WorktreeContext hierarchy
    And parent-child relationships should be derived from parentWorktreeId fields
    And no database lookup should be necessary

  @worktrees @resumability
  Scenario: Execution can resume from planning specs after interruption
    Given a goal execution was interrupted partway through
    And worktrees were created with planning specs committed
    When execution resumes from the same repoRoot
    Then the system should discover existing worktrees by reading .multi-agent-plan files
    And WorktreeContext objects should be reconstructed from planning specs
    And execution should continue from the last checkpoint
    And no loss of worktree structure or state should occur

  @worktrees @plan_spec_format
  Scenario Outline: Planning spec stores node metadata for reconstruction
    Given a WorkNode with various attributes
    When the planning spec is created
    Then the .multi-agent-plan file should be in <format>
    And all required fields should be present and valid
    And the spec should be human-readable for debugging
    And the spec should be parseable by the execution engine

    Examples:
      | format |
      | JSON   |
      | YAML   |
      | TOML   |

  @worktrees @plan_update
  Scenario: Planning spec can be updated as node state changes
    Given a worktree with initial planning spec
    When the WorkNode prompt is edited
    Then the planning spec should be updated with new prompt
    And the update should be committed to git
    And the commit message should indicate what changed (e.g., "Update prompt in planning spec")
    And the old version remains in git history

  @worktrees @git_native
  Scenario: Complete worktree history is accessible via git
    Given a worktree that has been created, modified, and completed
    When the git log is examined
    Then the initial planning spec commit should be visible
    And all work commits should follow
    And the merge commit (if merged to parent) should follow
    And the complete worktree lifecycle should be reconstructible from git history
