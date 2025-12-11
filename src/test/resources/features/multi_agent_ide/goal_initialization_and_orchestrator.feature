@multi_agent_ide @initialization @orchestrator
Feature: Goal Initialization and Orchestrator Setup
  As the orchestration system
  I want to initialize computation graphs from user goals
  So that the multi-agent system has a clear starting point with proper worktree and spec setup

  Background:
    Given the multi-agent-ide test environment is initialized with:
      | parameter                   | value           |
      | dockerComposePath           | <composePath>   |
      | serviceToStart              | multi-agent-ide |
      | eventSubscriptionType       | websocket       |
      | testEventListenerSubscribed | all_events      |

  @initialization @core
  Scenario: OrchestratorNode is created and initialized
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                               |
      | orch-1 | ORCHESTRATOR | READY  | null     | work-A   | Build a REST API for user management |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile             |
      | NODE_ADDED          | ORCHESTRATOR | orchestrator-added.json |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | orchestrator-ready.json |
    And the mock response file ""
    When the orchestrator node is initialized with the goal
    Then the expected events should have been received
    And a new OrchestratorNode should exist with status READY
    And the OrchestratorNode should store the original goal prompt

  @initialization @base_worktree
  Scenario: Base worktree is created for main repository
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                  |
      | orch-1 | ORCHESTRATOR | READY  | null     | work-A   | Implement data pipeline |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile                |
      | NODE_ADDED          | ORCHESTRATOR | orchestrator-added.json    |
      | WORKTREE_CREATED    | ORCHESTRATOR | main-worktree-created.json |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | orchestrator-ready.json    |
    And the mock response file ""
    When the orchestrator node is initialized with the goal
    Then the expected events should have been received
    And a git worktree should be created for the main repository
    And the worktree should be checked out at the base branch

  @initialization @submodule_worktrees
  Scenario: Worktrees are created for all git submodules
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                       |
      | orch-1 | ORCHESTRATOR | READY  | null     | work-A   | Build microservices platform |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile                     |
      | NODE_ADDED          | ORCHESTRATOR | orchestrator-added.json         |
      | WORKTREE_CREATED    | ORCHESTRATOR | main-worktree-created.json      |
      | WORKTREE_CREATED    | ORCHESTRATOR | auth-submodule-worktree.json    |
      | WORKTREE_CREATED    | ORCHESTRATOR | payment-submodule-worktree.json |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | orchestrator-ready.json         |
    And the mock response file ""
    When the orchestrator node is initialized with submodule repository
    Then the expected events should have been received
    And worktrees should be created for each submodule
    And each submodule worktree should be at the correct commit

  @initialization @spec_creation
  Scenario: Base spec file is created with required sections
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt            |
      | orch-1 | ORCHESTRATOR | READY  | null     | work-A   | Create mobile app |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile                |
      | NODE_ADDED          | ORCHESTRATOR | orchestrator-added.json    |
      | WORKTREE_CREATED    | ORCHESTRATOR | main-worktree-created.json |
      | SPEC_CREATED        | ORCHESTRATOR | spec-created.json          |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | orchestrator-ready.json    |
    And the mock response file ""
    When the orchestrator node is initialized with the goal
    Then the expected events should have been received
    And a spec file should exist in the main worktree
    And the spec should contain Header section with metadata
    And the spec should contain Plan section
    And the spec should contain Status section
    And the spec should be in markdown format

  @initialization @spec_structure
  Scenario: Spec includes submodule sections for multi-module repositories
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                    |
      | orch-1 | ORCHESTRATOR | READY  | null     | work-A   | Build e-commerce platform |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile                |
      | NODE_ADDED          | ORCHESTRATOR | orchestrator-added.json    |
      | WORKTREE_CREATED    | ORCHESTRATOR | main-worktree-created.json |
      | SPEC_CREATED        | ORCHESTRATOR | spec-created.json          |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | orchestrator-ready.json    |
    And the mock response file ""
    When the orchestrator node is initialized with submodule repository
    Then the expected events should have been received
    And the spec should contain Submodules section
    And the Submodules section should list all submodule names
    And each submodule should have its own Plan and Status subsections

  @initialization @hierarchy
  Scenario: Multiple OrchestratorNodes can be initialized independently
    Given multiple computation graphs with the following structure:
      | graphId | nodeId | nodeType     | status | parentId | children | prompt               |
      | goal-A  | orch-A | ORCHESTRATOR | READY  | null     | work-A1  | Build search service |
      | goal-B  | orch-B | ORCHESTRATOR | READY  | null     | work-B1  | Build payment system |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile               |
      | NODE_ADDED          | ORCHESTRATOR | orchestrator-a-added.json |
      | WORKTREE_CREATED    | ORCHESTRATOR | worktree-a-created.json   |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | orchestrator-a-ready.json |
      | NODE_ADDED          | ORCHESTRATOR | orchestrator-b-added.json |
      | WORKTREE_CREATED    | ORCHESTRATOR | worktree-b-created.json   |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | orchestrator-b-ready.json |
    And the mock response file ""
    When both orchestrator nodes are initialized
    Then the expected events should have been received
    And OrchestratorNode orch-A should have independent worktrees
    And OrchestratorNode orch-B should have independent worktrees
    And each node should have its own base spec file

  @initialization @idempotency
  Scenario: Re-initializing an OrchestratorNode is idempotent
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt             |
      | orch-1 | ORCHESTRATOR | READY  | null     | work-A   | Build search index |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile                |
      | NODE_ADDED          | ORCHESTRATOR | orchestrator-added.json    |
      | WORKTREE_CREATED    | ORCHESTRATOR | main-worktree-created.json |
      | SPEC_CREATED        | ORCHESTRATOR | spec-created.json          |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | orchestrator-ready.json    |
    And the mock response file ""
    When the orchestrator node is initialized with the goal
    And the orchestrator node is initialized again with the same goal
    Then the expected events should have been received exactly once
    And the worktree should not be recreated
    And the spec file should not be duplicated

  @initialization @goal_storage
  Scenario: Original goal prompt is preserved in spec and node metadata
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                                  |
      | orch-1 | ORCHESTRATOR | READY  | null     | work-A   | Implement real-time notification system |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile             |
      | NODE_ADDED          | ORCHESTRATOR | orchestrator-added.json |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | orchestrator-ready.json |
    And the mock response file ""
    When the orchestrator node is initialized with the goal
    Then the expected events should have been received
    And the OrchestratorNode should store the original goal prompt in metadata
    And the spec file should include the goal description in the Header section
    And the spec should include the exact prompt text as the goal statement

  @initialization @branch_setup
  Scenario: Base branch is correctly set for worktrees
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                 |
      | orch-1 | ORCHESTRATOR | READY  | null     | work-A   | Build analytics engine |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile                |
      | NODE_ADDED          | ORCHESTRATOR | orchestrator-added.json    |
      | WORKTREE_CREATED    | ORCHESTRATOR | main-worktree-created.json |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | orchestrator-ready.json    |
    And the mock response file ""
    When the orchestrator node is initialized with the goal
    Then the expected events should have been received
    And the main worktree should be checked out at the base branch
    And the base branch should default to "main"
    And the initial commit hash should be recorded

  @initialization @empty_status
  Scenario: Spec Status section is empty at initialization
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt             |
      | orch-1 | ORCHESTRATOR | READY  | null     | work-A   | Build chat service |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile             |
      | NODE_ADDED          | ORCHESTRATOR | orchestrator-added.json |
      | SPEC_CREATED        | ORCHESTRATOR | spec-created.json       |
      | NODE_STATUS_CHANGED | ORCHESTRATOR | orchestrator-ready.json |
    And the mock response file ""
    When the orchestrator node is initialized with the goal
    Then the expected events should have been received
    And the spec Status section should be empty or contain only initialization message
    And the spec Plan section should be ready for agent updates
