@multi_agent_ide @branching @worktrees
Feature: Branching Nodes with Submodules
  As the orchestration system
  I want to support branching of nodes with independent worktrees
  So that users can explore alternative implementations and maintain parallel work streams

  Background:
    Given the multi-agent-ide test environment is initialized with:
      | parameter                   | value           |
      | dockerComposePath           | <composePath>   |
      | serviceToStart              | multi-agent-ide |
      | eventSubscriptionType       | websocket       |
      | testEventListenerSubscribed | all_events      |

  @branching @core
  Scenario: Node can be branched to explore alternative implementations
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build search service |
    And the expected events for this scenario are:
    And the mock response file ""
    When the user branches work-A to explore alternative implementation
    Then the expected events should have been received
    And a new branched WorkNode should be created with parent reference to work-A
    And a new worktree should be created for the branched node
    And the new worktree should be independent of the original
    And both nodes should coexist in the graph

  @branching @with_submodules
  Scenario: Branched nodes create independent worktrees for all submodules
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build payment system |
    And the expected events for this scenario are:
    And the mock response file ""
    Then the expected events should have been received
    And a WorkNode should be created for the original work
    And worktrees should be created for main repo and all submodules
    And a branched WorkNode should be created with parent reference
    And independent worktrees should be created for the branch main repo and submodules
    And branched submodule worktrees should reference their parent main worktree

  @branching @spec_creation
  Scenario: Branched nodes create independent specs
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build authentication |
    And the expected events for this scenario are:
    And the mock response file ""
    When the user branches work-C
    Then the expected events should have been received
    And a WorkNode should be created with its own spec
    And a branched WorkNode should be created
    And the branched node should have its own spec file
    And branched spec should be initialized with same sections as original
    And branched spec should be independent for modifications

  @branching @prompt_modification
  Scenario: Branched node can have modified prompt/goal
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt            |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build API service |
    And the expected events for this scenario are:
    And the mock response file ""
    When the user branches work-D with modified goal
    Then the expected events should have been received
    And a branched WorkNode should be created
    And the branched node should store the modified goal
    And the new goal should be distinct from the original
    And agents can use the alternative goal for execution

  @branching @parallel_branches
  Scenario: Multiple branches can exist simultaneously
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt              |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build data pipeline |
    And the expected events for this scenario are:
    And the mock response file ""
    When the user creates three branches of work-E
    Then the expected events should have been received
    And a WorkNode should be created
    And three branched WorkNodes should be created as siblings
    And each branch should have independent worktrees
    And each branch should have independent specs
    And all branches should be able to execute in parallel

  @branching @branch_relationships
  Scenario: Branch relationships are maintained in node hierarchy
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                 |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build messaging system |
    And the expected events for this scenario are:
    And the mock response file ""
    When the user branches work-F
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And a branched WorkNode should be created with parent reference to work-F
    And the parentNodeId should be explicitly stored in branched node
    And the relationship should be bidirectional for graph traversal
    And both nodes should remain visible in the computation graph

  @branching @worktree_separation
  Scenario: Branched worktrees remain completely isolated
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt            |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build cache layer |
    And the expected events for this scenario are:
    And the mock response file ""
    When the user branches work-G
    Then the expected events should have been received
    And a WorkNode should be created with its worktree
    And a branched WorkNode should be created with independent worktree
    And modifications to original worktree should not affect branch
    And modifications to branch worktree should not affect original
    And worktrees should not share any files

  @branching @independent_execution
  Scenario: Branched nodes can be executed independently
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                  |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build logging framework |
    And the expected events for this scenario are:
    And the mock response file ""
    When both work-H and work-H-alt execute in parallel
    Then the expected events should have been received
    And both WorkNodes should be created
    And both should transition to RUNNING independently
    And streaming deltas should be emitted for both in parallel
    And modifications to one should not block the other
    And both can complete independently

  @branching @submodule_pointer_independence
  Scenario: Branched submodule worktrees have independent pointers
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                  |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Integrate microservices |
    And the expected events for this scenario are:
    And the mock response file ""
    When the user branches work-I
    Then the expected events should have been received
    And a WorkNode should be created with submodule worktrees
    And a branched WorkNode should be created with independent submodule worktrees
    And updating submodule pointer in original should not affect branch
    And updating submodule pointer in branch should not affect original
    And each branch can checkout different submodule commits independently

  @branching @ancestry_tracking
  Scenario: Branch ancestry is tracked for UI display and resumability
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                  |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build recommendation ML |
    And the expected events for this scenario are:
      | eventType     | sourceNodeId   | targetNodeId | payloadFile                  |
      | NODE_ADDED    | work-J         | orch-1       | work-j-node-added.json       |
      | NODE_BRANCHED | work-J-alt1    | work-J       | work-j-alt1-branched.json    |
      | NODE_BRANCHED | work-J-alt2    | work-J       | work-j-alt2-branched.json    |
      | NODE_BRANCHED | work-J-alt1-v2 | work-J-alt1  | work-j-alt1-v2-branched.json |
    And the mock response file ""
    When branches are created and re-branched
    Then the expected events should have been received
    And work-J-alt1 should have parentNodeId=work-J
    And work-J-alt2 should have parentNodeId=work-J
    And work-J-alt1-v2 should have parentNodeId=work-J-alt1
    And the ancestry chain should be traversable
    And UI can display the branching tree correctly
