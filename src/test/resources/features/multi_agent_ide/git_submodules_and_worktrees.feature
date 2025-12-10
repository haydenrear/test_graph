@multi_agent_ide @submodules @worktrees @git
Feature: Git Submodules and Recursive Worktree Management
  As the orchestration system
  I want to manage repositories with git submodules
  So that agents can work on main repo and multiple submodules with isolated worktrees

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events
    And git is properly configured in the container

  @submodules @initialization @core
  Scenario: WorkNode detects submodules and creates worktrees for each
    Given a git repository with submodules:
      | name      | path          |
      | auth-lib  | libs/auth     |
      | utils-lib | libs/utils    |
    And a WorkNode is created for this repository
    When the WorkNode transitions to READY and initializes worktrees
    Then a main WorktreeContext should be created for the repository
    And a WorktreeContext should be created for each submodule:
      | submodule | worktreePath              |
      | auth-lib  | {repoRoot}/.worktrees/auth-lib-{nodeId} |
      | utils-lib | {repoRoot}/.worktrees/utils-lib-{nodeId} |
    And each submodule worktree should be checked out at correct commit
    And WorktreeCreatedEvent should be emitted for main + each submodule
    And the WorkNode payload should reference all WorktreeContexts

  @submodules @hierarchy
  Scenario: Submodule worktrees form hierarchy parallel to main worktree hierarchy
    Given a parent WorkNode with 2 submodules
    And the parent creates 3 child WorkNodes
    When all child WorkNodes initialize their worktrees
    Then the main repo worktree hierarchy should be:
      | parent main worktree |
      | ├─ child 1 main worktree |
      | ├─ child 2 main worktree |
      | └─ child 3 main worktree |
    And for each submodule (e.g., auth-lib), the hierarchy should be:
      | parent auth-lib worktree |
      | ├─ child 1 auth-lib worktree |
      | ├─ child 2 auth-lib worktree |
      | └─ child 3 auth-lib worktree |
    And each worktree's parentWorktreeId should correctly reference its parent
    And the two hierarchies should mirror each other

  @submodules @spec_creation
  Scenario: Spec files track submodule work alongside main repo work
    Given a WorkNode with main repo and 2 submodules
    When planning creates the WorkNode and initializes worktrees
    Then the main worktree spec should contain:
      | section        | content                        |
      | Plan           | Main repo work items           |
      | Status         | Progress for main repo         |
      | Submodules     | References to submodule specs  |
    And each submodule spec should contain:
      | section | content                   |
      | Plan    | Work items for submodule  |
      | Status  | Progress for submodule    |
    And submodule spec paths should be documented in main spec's Submodules section

  @submodules @editing_main_only
  Scenario: Editor agent can edit only main repo without touching submodules
    Given a WorkNode with main repo and submodules
    And the prompt specifies changes to main repo only
    When EditorGraphAgent executes with streaming
    Then files should be modified only in main worktree
    And submodule worktrees should remain unchanged
    And the main spec should be updated with progress
    And submodule specs should not be modified
    And NodeStreamDeltaEvent should stream only main repo edits

  @submodules @editing_submodule_only
  Scenario: Editor agent can edit only one or more submodules
    Given a WorkNode with main repo and 3 submodules (auth-lib, utils, config)
    And the prompt specifies changes to auth-lib and utils only
    When EditorGraphAgent executes with streaming
    Then files should be modified in auth-lib and utils submodule worktrees
    And the config submodule worktree should remain unchanged
    And main repo files should remain unchanged
    And main spec should be updated indicating submodule edits
    And submodule specs for auth-lib and utils should be updated
    And config submodule spec should be unchanged

  @submodules @editing_main_and_submodules
  Scenario: Editor agent can edit main repo and submodules in same execution
    Given a WorkNode with main repo and 2 submodules
    And the prompt requires changes across main and both submodules
    When EditorGraphAgent executes with streaming
    Then modifications should occur in:
      | location              | files_affected   |
      | Main repo worktree    | 3 files          |
      | auth-lib submodule    | 2 files          |
      | utils-lib submodule   | 1 file           |
    And the main spec should reflect all changes
    And submodule specs should reflect submodule-specific changes
    And streaming should indicate which component is being edited
    And NodeStreamDeltaEvent should show progress across all components

  @submodules @submodule_pointer_update
  Scenario: Editing submodule updates submodule pointer in main repo
    Given a WorkNode with main repo and a submodule
    When the submodule is edited and changes are committed
    Then the submodule worktree's HEAD should advance to new commit
    And the main worktree's submodule pointer should be updated
    And committing in main worktree should record the new submodule pointer
    And git status in main worktree should show "modified: path/to/submodule"

  @submodules @merging_main_only
  Scenario: Merging child worktree with no submodule changes
    Given a parent WorkNode with submodules
    And a child WorkNode that only modified main repo files
    When merge is initiated
    Then the main repo changes should be merged into parent's main worktree
    And submodule worktrees should be unaffected
    And submodule pointers in parent should remain stable
    And merge should be clean (no conflicts in submodule pointers)
    And WorktreeMergedEvent should indicate successful merge

  @submodules @merging_submodule_only
  Scenario: Merging child worktree with only submodule changes
    Given a parent WorkNode with submodules
    And a child WorkNode that only modified auth-lib submodule
    When merge is initiated
    Then changes in auth-lib worktree should be merged into parent's auth-lib worktree
    And main repo worktree should have updated submodule pointer
    And other submodules should be unaffected
    And WorktreeMergedEvent should confirm successful merge

  @submodules @merging_main_and_submodules
  Scenario: Merging with changes in main repo and multiple submodules
    Given a parent WorkNode with main repo and 2 submodules
    And a child with edits across main, auth-lib, and utils-lib
    When merge is initiated
    Then all three components should merge concurrently:
      | component        | action                                  |
      | Main worktree    | Merge changes into parent main          |
      | auth-lib         | Merge changes into parent auth-lib      |
      | utils-lib        | Merge changes into parent utils-lib     |
    And main repo submodule pointers should be updated
    And all three WorktreeMergedEvent should be emitted
    And specs should merge: child main spec into parent, child auth-lib spec into parent auth-lib spec, etc.

  @submodules @merge_conflicts_main
  Scenario: Merge conflict in main repo is detected and handled
    Given a parent and child with conflicting main repo changes
    And submodules have no conflicts
    When merge is attempted
    Then the merge should pause with conflict in main repo
    Then NodeStatusChangedEvent should indicate WAITING_REVIEW or FAILED
    And HumanReviewNode should be created for conflict resolution
    And submodule merges should either wait or be rolled back
    And user can resolve conflict and retry merge

  @submodules @merge_conflicts_submodule
  Scenario: Merge conflict in submodule is detected and handled
    Given a parent and child both edited the same submodule
    And conflicting changes exist in submodule
    When merge is attempted
    Then merge should pause with conflict in submodule worktree
    And NodeStatusChangedEvent should indicate WAITING_REVIEW
    And main repo merge should be paused or rolled back
    And HumanReviewNode should allow resolving submodule conflict
    And resolution should update submodule pointer correctly in main repo

  @submodules @merge_pointer_consistency
  Scenario: Submodule pointers remain consistent during merge
    Given a parent with submodule at commit ABC
    And a child that updated submodule to commit DEF
    When merge completes successfully
    Then parent main worktree submodule pointer should be at commit DEF
    And parent's .gitmodules should not be modified (unless required)
    And git status should show "modified: path/to/submodule"
    And the merge commit should record the new pointer

  @submodules @branching
  Scenario: Branching WorkNode branches main repo and all submodules
    Given a WorkNode with main repo and 2 submodules
    When a branch request is sent
    Then new WorkNode should be created
    And main repo should be branched to new working branch
    And each submodule should also be branched to corresponding branch
    And WorktreeBranchedEvent should be emitted for main + each submodule
    And branch-specific specs should be created for main and each submodule
    And parent-child worktree relationships should be preserved

  @submodules @pruning
  Scenario: Pruning WorkNode prunes all associated worktrees
    Given a WorkNode with main repo and 3 submodules, plus descendants
    And multiple levels of child nodes with their own worktrees
    When pruning is requested for the parent node
    Then main repo worktree should be marked DISCARDED
    And each submodule worktree should be marked DISCARDED
    And all descendant worktrees (main + submodules) should be DISCARDED
    And WorktreeDiscardedEvent should be emitted for all
    And specs should be archived in git

  @submodules @parallel_execution
  Scenario: Multiple nodes can operate on same repo with submodules in parallel
    Given a goal with main repo and 2 submodules
    And two independent WorkNodes: A and B (no dependencies)
    When both transition to RUNNING
    Then both should create separate worktrees for main and each submodule
    And node A edits main repo and auth-lib
    And node B edits only utils-lib
    And both should stream independently
    And their edits should not interfere
    And each produces independent diffs
    And both can be merged independently or sequentially

  @submodules @worktree_discovery
  Scenario: Worktrees for submodules are discoverable via planning specs
    Given a partially completed execution with main + submodule worktrees
    When discovery scan is run
    Then .multi-agent-plan files in each worktree should be found
    And parentWorktreeId in submodule specs should link them to parent worktrees
    And the complete hierarchy (main + submodules) should be reconstructed
    And no database required

  @submodules @resumability
  Scenario: Execution resumes with correct submodule state
    Given a partially completed execution with main and submodule edits
    And some nodes COMPLETED, some RUNNING, some PENDING
    When execution resumes after interruption
    Then worktrees should be discovered via planning specs
    And submodule worktrees should be at correct commits/branches
    And specs should reflect which submodules have completed work
    And execution should resume from next READY/RUNNING node
    And submodule state should be consistent with spec files

  @submodules @submodule_conflicts_resumability
  Scenario: Resume correctly handles unresolved submodule merge conflicts
    Given a previous merge left conflict markers in submodule
    And the merge state is recorded in spec Status section
    When execution resumes
    Then the conflict should be detected from spec Status
    And the conflict should remain in the worktree for manual resolution
    And execution should wait for WAITING_REVIEW to be resolved
    And no automatic continuation past conflict

  @submodules @git_submodule_commands
  Scenario: Editor agents use proper git submodule commands
    Given a WorkNode with submodules
    And EditorGraphAgent is editing
    When operations on submodules occur
    Then the agent should use:
      | command/operation           | context                           |
      | git submodule update        | After pulling parent changes      |
      | Commit in submodule         | Then update pointer in parent     |
      | git add <submodule_path>    | To stage submodule pointer update |
    And submodule operations should be idempotent and safe
    And .gitmodules should not be accidentally modified
    And submodule URLs should not change

  @submodules @spec_submodule_sections
  Scenario: Specs correctly document submodule-specific work
    Given a WorkNode with 3 submodules
    When spec is created or updated with submodule edits
    Then main spec should have #/Submodules section with:
      | item                | example                              |
      | Submodule list      | auth-lib, utils-lib, config-lib      |
      | Completion by module| auth-lib: 80%, utils-lib: 30%, ...   |
      | Links to sub-specs  | Paths to submodule-specific specs    |
    And agents should use get_section(spec, "#/Submodules") to understand submodule work
    And when merging, submodule sections should be intelligently merged
