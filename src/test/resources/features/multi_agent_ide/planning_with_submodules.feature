@multi_agent_ide @planning @submodules
Feature: Planning with Git Submodules
  As the orchestration system
  I want planning agents to break goals into tickets that account for main repo and submodule work
  So that multi-module systems are coordinated properly

  Background:
    Given the multi-agent-ide test environment is initialized with:
      | parameter                   | value           |
      | dockerComposePath           | <composePath>   |
      | serviceToStart              | multi-agent-ide |
      | eventSubscriptionType       | websocket       |
      | testEventListenerSubscribed | all_events      |

  @planning @core
  Scenario: PlanningGraphAgent breaks goal into work tickets
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build a REST API for user management |
    And the expected events for this scenario are:
    And the mock response file ""
    When the PlanningGraphAgent executes
    Then the expected events should have been received
    And a PlanningNode should be created as child of OrchestratorNode
    And three WorkNodes should be created as children of PlanningNode
    And each WorkNode should have a distinct goal
    And each WorkNode should reference the original goal in its metadata

  @planning @recursive_worktrees
  Scenario: Child WorkNodes create recursive worktrees for main repo and submodules
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build a REST API for user management |
    And the expected events for this scenario are:
    And the mock response file ""
    When the PlanningGraphAgent executes with submodule repository
    Then the expected events should have been received
    And each WorkNode should be created as child of PlanningNode
    And each child WorkNode should have its own main worktree
    And each child WorkNode should have worktrees for all submodules
    And all submodule worktrees should be linked to their parent WorkNode

  @planning @worktree_hierarchy
  Scenario: Child worktrees have parent worktree references forming a hierarchy
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build a REST API for user management |
    And the expected events for this scenario are:
    And the mock response file ""
    When the PlanningGraphAgent creates child WorkNodes
    Then the expected events should have been received
    And each WorkNode should have a parentWorktreeId referencing its parent
    And the worktree hierarchy should mirror the node hierarchy
    And parent-child relationships should be bidirectional

  @planning @spec_initialization
  Scenario: Specs are created for each WorkNode with plan and status sections
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build a REST API for user management |
    And the expected events for this scenario are:
    And the mock response file ""
    When the PlanningGraphAgent creates WorkNodes
    Then the expected events should have been received
    And a PlanningNode should be created
    And a WorkNode should be created as child of PlanningNode
    And a spec file should be created in each WorkNode's main worktree
    And the spec should contain the WorkNode's goal
    And the spec Plan section should be empty and ready for agent updates
    And the spec Status section should indicate initialization

  @planning @submodule_specs
  Scenario: Specs include submodule sections describing expected work per submodule
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build a REST API for user management |
    And the expected events for this scenario are:
    And the mock response file ""
    When the PlanningGraphAgent creates WorkNodes with submodules
    Then the expected events should have been received
    And a PlanningNode should be created
    And a WorkNode should be created
    And the spec should contain a Submodules section
    And each submodule should have its own Plan and Status subsections
    And the Submodules section should describe what changes are expected per submodule

  @planning @goal_decomposition
  Scenario: Goals are decomposed into clear, actionable subtasks
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                                                   |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build a REST API for user management with authentication |
    And the expected events for this scenario are:
    And the mock response file ""
    When the PlanningGraphAgent decomposes the goal
    Then the expected events should have been received
    And a PlanningNode should be created
    And three WorkNodes should be created as children
    And work-A should focus on API structure and endpoints
    And work-B should focus on authentication and authorization
    And work-C should focus on database integration
    And each WorkNode prompt should be a clear subtask of the original goal

  @planning @parallel_execution
  Scenario: Multiple planning agents can operate in parallel on independent goals
    Given multiple computation graphs with the following structure:
      | graphId | nodeId | nodeType     | status | parentId | children | prompt               |
      | goal-A  | orch-A | ORCHESTRATOR | READY  | null     | null     | Build search service |
      | goal-B  | orch-B | ORCHESTRATOR | READY  | null     | null     | Build payment system |
    And the expected events for this scenario are:
    And the mock response file ""
    When both PlanningGraphAgents execute in parallel
    Then the expected events should have been received
    And PlanningNode plan-A should be created for orch-A
    And WorkNode work-A1 should be created independently of work-B1
    And PlanningNode plan-B should be created for orch-B
    And both planning nodes should complete without interference
    And each goal should have its own set of independent WorkNodes

  @planning @prompt_preservation
  Scenario: WorkNode prompts preserve original goal context
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                                                     |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build a real-time multi-user collaborative document editor |
    And the expected events for this scenario are:
    And the mock response file ""
    When the PlanningGraphAgent creates WorkNodes
    Then the expected events should have been received
    And a PlanningNode should be created
    And a WorkNode should be created as child of PlanningNode
    And each WorkNode should store the original orchestrator goal
    And WorkNode prompts should reference the broader goal context
    And agents executing on WorkNodes can reconstruct the full goal hierarchy

  @planning @status_tracking
  Scenario: Planning progress is tracked in orchestrator spec
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build a REST API for user management |
    And the expected events for this scenario are:
    And the mock response file ""
    When the PlanningGraphAgent completes execution
    Then the expected events should have been received
    And a PlanningNode should be created as child of OrchestratorNode
    And WorkNodes should be created as children of PlanningNode
    And the PlanningNode should transition to COMPLETED
    And the orchestrator spec Status section should reflect planning completion
    And the orchestrator spec should list all created WorkNodes
    And child WorkNode specs should be referenced in the orchestrator spec
