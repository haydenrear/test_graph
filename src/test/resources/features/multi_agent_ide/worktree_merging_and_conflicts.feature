@multi_agent_ide @worktrees @merging
Feature: Worktree Merging and Conflict Resolution
  As the orchestration system
  I want to merge child worktrees back into parent worktrees
  So that completed work is integrated and the hierarchy can be collapsed

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events
    And git is properly configured in the container

  @merging @core
  Scenario: Child worktree is merged into parent worktree
    Given a parent WorkNode with worktree and branch "main"
    And a child WorkNode with worktree branched from parent
    And the child has completed work with committed changes
    When a merge request is sent via message
    Then the child worktree's branch should be merged into parent's "main" branch
    And the parent worktree should contain child's changes
    And a WorktreeMergedEvent should be emitted
    And the merge commit should be recorded in parent worktree's git log

  @merging @no_conflicts
  Scenario: Clean merge when no conflicts exist
    Given parent worktree with file A.py
    And child worktree with changes only to B.py
    When merge is executed
    Then the merge should complete successfully
    And parent worktree should contain both A.py and modified B.py
    And no merge conflicts should be detected
    And NodeStatusChangedEvent should indicate successful merge

  @merging @conflict_detection
  Scenario: Merge conflicts are detected and reported
    Given parent worktree with edits to config.json
    And child worktree with conflicting edits to config.json
    When merge is attempted
    Then git should detect merge conflict
    And merge should pause in CONFLICTED state
    And a NodeStatusChangedEvent should indicate WAITING_REVIEW with conflict info
    And conflict markers should appear in the conflicted files
    And NodeUpdatedEvent should contain conflict details

  @merging @manual_resolution
  Scenario: Human review node handles merge conflict resolution
    Given a merge is in CONFLICTED state
    And a HumanReviewNode is created for conflict resolution
    When the reviewer resolves conflicts manually via message
    Then the conflict markers should be removed
    And a resolution message should be sent
    Then the merge should be completed
    And NodeStatusChangedEvent should indicate merge completion
    And WorktreeMergedEvent should be emitted

  @merging @auto_resolution
  Scenario: Simple merge conflicts can be auto-resolved with strategy
    Given a merge conflict exists in a non-critical file
    And an auto-resolution strategy is configured (e.g., "ours", "theirs", "union")
    When the strategy is applied
    Then the conflict should be resolved automatically
    And the chosen version should be used
    And merge should complete without manual intervention
    And NodeUpdatedEvent should record the resolution strategy used

  @merging @cascade_upward
  Scenario: Merged child results propagate upward through hierarchy
    Given a deep hierarchy: Grandparent -> Parent -> Child worktrees
    And Child completes and merges into Parent
    When Parent merges into Grandparent
    Then Grandparent should contain all changes from both Child and Parent
    And the complete change history should be preserved
    And three WorktreeMergedEvent should be emitted (one per merge)

  @merging @partial_merge
  Scenario: Selective merge of specific commits or files
    Given a child worktree with multiple commits
    And only some commits should be merged to parent
    When a selective merge is requested with commit filter
    Then only specified commits should be merged into parent
    And unwanted commits should remain in child worktree
    And NodeUpdatedEvent should record which commits were merged

  @merging @stale_parent
  Scenario: Merge handles parent worktree that changed since child was created
    Given a parent worktree at commit A
    And a child worktree created branching from A
    And parent worktree advances to commit B (other work)
    And child has work to merge
    When merge is attempted
    Then the child's commits should be rebased onto B (if using rebase strategy)
    Or the merge should create a merge commit combining both (if using merge strategy)
    And the result should include both parent and child changes

  @merging @three_way_merge
    Scenario: Three-way merge resolves correctly with common ancestor
    Given parent and child diverged from a common commit
    And parent has changes X, child has changes Y
    When three-way merge is performed
    Then the common ancestor should be identified
    And changes X and Y should be combined if non-conflicting
    And conflicts should only appear where both changed same lines
    And merge result should be coherent

  @merging @merge_status_tracking
  Scenario: Merge status is tracked in WorktreeContext
    Given a worktree about to be merged
    When merge begins
    Then WorktreeContext.status should transition from ACTIVE to MERGING (or similar)
    And WorktreeMergedEvent should be emitted with successful merge
    And WorktreeContext.status should become MERGED
    And the planning spec should be updated to reflect merged status

  @merging @post_merge_cleanup
  Scenario: Post-merge, child worktree is marked as complete
    Given a successful merge has occurred
    And the child worktree changes are now in parent
    When post-merge cleanup is triggered
    Then the child WorktreeContext should be marked MERGED
    And the child worktree directory may optionally be retained or cleaned
    And references in planning spec should mark merge completion
    And dependent nodes should be able to proceed

  @merging @failed_merge_recovery
  Scenario: Failed merge can be retried or rolled back
    Given a merge attempt that failed
    When rollback is requested
    Then the parent worktree should revert to pre-merge state
    And child worktree should remain intact
    And NodeStatusChangedEvent should indicate FAILED
    And a new merge attempt can be initiated

  @merging @merge_verification
  Scenario: Merge results are verified before completion
    Given a merge has been executed
    When verification is triggered
    Then the merged worktree should be checked for consistency
    And build or lint checks may be run (mocked if needed)
    And if verification fails, merge should be marked FAILED
    And if verification succeeds, WorktreeMergedEvent confirms completion

  @merging @parallel_merges
  Scenario: Multiple independent merges can occur in parallel
    Given parent worktree with children A and B
    And both A and B have completed work ready to merge
    When merge requests are sent for both
    Then both merges should proceed concurrently if possible
    And no race conditions should corrupt either merge
    And both WorktreeMergedEvent should be emitted
    And parent should contain merged changes from both A and B
