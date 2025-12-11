@multi_agent_ide @merging @submodules
Feature: Merging Worktrees with Submodules
  As the orchestration system
  I want to merge child worktrees back into parent worktrees
  So that work can be integrated while properly handling submodule commits and pointers

  Background:
    Given the multi-agent-ide test environment is initialized with:
      | parameter                   | value           |
      | dockerComposePath           | <composePath>   |
      | serviceToStart              | multi-agent-ide |
      | eventSubscriptionType       | websocket       |
      | testEventListenerSubscribed | all_events      |

  @merging @main_repo_only
  Scenario: Main repository changes are merged from child to parent worktree
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt            |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build API service |
    And the expected events for this scenario are:
    And the mock response file ""
    When the merge is requested from work-A into orchestrator
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And the WorkNode should transition to COMPLETED
    And main repo changes from child worktree should be merged to parent
    And the merge should fast-forward if no conflicts
    And parent worktree should reflect all child changes

  @merging @submodule_changes
  Scenario: Submodule changes are merged with updated pointers
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                    |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Integrate payment service |
    And the expected events for this scenario are:
    And the mock response file ""
    When the merge is requested from work-B into orchestrator
    Then the expected events should have been received
    And a WorkNode should be created
    And submodule worktree changes should be merged to parent submodule
    And the main repo submodule pointer should be updated
    And the pointer should reference the new submodule commit
    And the merge should be atomic

  @merging @main_and_submodules
  Scenario: Both main repo and multiple submodule changes are merged together
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                              |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Integrate auth and payment services |
    And the expected events for this scenario are:
    And the mock response file ""
    When the merge is requested from work-C into orchestrator
    Then the expected events should have been received
    And a WorkNode should be created with main and submodule worktrees
    And main repo changes should be merged
    And auth-api submodule changes should be merged
    And payment-api submodule changes should be merged
    And both submodule pointers in main repo should be updated
    And all changes should be atomic

  @merging @conflict_detection
  Scenario: Merge conflicts are detected and reported
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build database layer |
    And the expected events for this scenario are:
    And the mock response file ""
    When the merge encounters conflicts
    Then the expected events should have been received
    And a WorkNode should be created
    And the merge should detect conflicting files
    And the system should report which files have conflicts
    And the work node status should transition to FAILED or WAITING_REVIEW
    And merge resolution should be required

  @merging @auto_resolution
  Scenario: Simple conflicts can be auto-resolved using resolution strategy
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                   |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Add configuration system |
    And the expected events for this scenario are:
    And the merge resolution strategy is set to "union"
    And the mock response file ""
    When the merge is resolved automatically
    Then the expected events should have been received
    And a WorkNode should be created
    And conflicts should be auto-resolved using the specified strategy
    And the merge should complete successfully
    And the node status should transition to COMPLETED

  @merging @conflict_review
  Scenario: Complex conflicts require human review for resolution
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Refactor core module |
    And the expected events for this scenario are:
    And the mock response file ""
    When complex conflicts require review
    Then the expected events should have been received
    And a WorkNode should be created
    And conflicts should be detected
    And a review node should be created for conflict resolution
    And the system should wait for human decision
    And the human can choose resolution strategy or manual merge

  @merging @spec_merge
  Scenario: Child spec is merged into parent spec during code merge
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt             |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build search index |
    And the expected events for this scenario are:
    And the mock response file ""
    When the merge is performed
    Then the expected events should have been received
    And a WorkNode should be created with its own spec
    And the child spec summary should be retrieved
    And the child spec should be merged into parent spec
    And the parent spec Plan section should be updated with child work
    And the parent spec Status section should reflect completion

  @merging @recursive_hierarchy
  Scenario: Merges propagate up recursive worktree hierarchy
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt              |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build full platform |
    And the expected events for this scenario are:
    And the mock response file ""
    When child work-H is merged into parent orchestrator
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And the WorkNode worktree should be merged into parent
    And if the parent has its own parent, merge should propagate up
    And the entire hierarchy should be updated consistently

  @merging @parallel_merges
  Scenario: Multiple independent merges can occur in parallel
    Given multiple computation graphs with the following structure:
      | graphId | nodeId | nodeType     | status | parentId | children | prompt                |
      | goal-A  | orch-A | ORCHESTRATOR | READY  | null     | null     | Build search feature  |
      | goal-B  | orch-B | ORCHESTRATOR | READY  | null     | null     | Build payment feature |
    And the expected events for this scenario are:
    And the mock response file ""
    When both work-A1 and work-B1 are merged in parallel
    Then the expected events should have been received
    And WorkNodes should be created on their respective orchestrators
    And both should transition to COMPLETED
    And merges should occur independently without blocking
    And both orchestrators should be updated with merged code

  @merging @submodule_pointer_sync
  Scenario: Submodule pointers are kept in sync across hierarchy
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build integrated app |
    And the expected events for this scenario are:
    And the mock response file ""
    When the merge is performed with submodule updates
    Then the expected events should have been received
    And a WorkNode should be created with submodule worktrees
    And submodule changes should be merged
    And the main repo submodule pointer should reference new commit
    And if parent has parent, its pointer should also be updated
    And pointer synchronization should cascade up the hierarchy

  @merging @merge_status_transition
  Scenario: Node status transitions correctly through merge process
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt            |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build new feature |
    And the expected events for this scenario are:
