@multi_agent_ide @planning
Feature: Planning and Work Generation
  As the orchestrator agent
  I want to break down goals into smaller WorkNodes
  So that distributed agents can execute subtasks in parallel

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events
    And LangChain4j models are mocked with predictable responses
    And mock LangChain4j planning response is configured
    And a test git repository is configured to be created with initial spec file
    And spec file configuration is set to standard markdown format

  @planning @core
  Scenario: Orchestrator agent creates planning tickets from goal
    When a new goal is created via message with description "Build a REST API for user management"
    And the orchestrator agent is triggered to plan
    Then a PlanningGraphAgent should execute
    And multiple WorkNode objects should be created as child nodes
    And NodeAddedEvent should be emitted for each new WorkNode
    And the WorkNodes should be READY for execution

  @planning @core
  Scenario: Planning tickets are hierarchically organized
    When a goal "Implement authentication system" is created
    And the orchestrator agent creates planning tickets
    Then the generated WorkNodes should be child nodes of the OrchestratorNode
    And each WorkNode should represent a distinct subtask
    And WorkNodes should not have circular dependencies

  @planning @parallelism
  Scenario: Multiple independent work nodes can be created
    When a goal "Refactor the codebase" is planned
    And the planner generates 3 independent tasks
    Then 3 WorkNodes should be created with status READY
    And all 3 nodes should be executable in parallel
    And NodeAddedEvent should be received for each node

  @planning @streaming
  Scenario: Planning process streams tokens as it generates tasks
    When a goal "Design data model" is created
    And the PlanningGraphAgent begins execution with streaming
    Then NodeStreamDeltaEvent should be emitted with planning tokens
    And the test listener should receive streaming tokens incrementally
    And the final WorkNodes should be persisted after streaming completes

  @planning @agent_execution
  Scenario Outline: Different agent types can participate in planning
    When a goal "<goal>" is created
    And the planning phase uses agent type "<agentType>"
    Then the appropriate agent should be invoked
    And WorkNodes should be created matching the agent's capabilities
    And the node structure should reflect the agent's planning style

    Examples:
      | goal                              | agentType           |
      | Write comprehensive test suite    | CodeAnalysisAgent   |
      | Optimize database performance     | PerformanceAgent    |
      | Implement caching strategy        | ArchitectureAgent   |

  @planning @annotations
  Scenario: Planning results can be annotated with metadata
    When a goal is planned and WorkNodes are created
    And the planning agent provides annotations
    Then each WorkNode should support Annotatable interface
    And annotations should be stored alongside the node
    And annotations should be retrievable via the event bus

  @planning @determinism
  Scenario: Planning with same goal produces consistent structure
    When goal "Add logging to API" is planned and nodes are captured
    And the same goal is planned again in a different execution
    Then both executions should produce WorkNodes with same structure
    And the number of nodes should be identical
    And the hierarchical relationships should match

  @planning @edge_cases
  Scenario: Planning very small goal creates minimal work nodes
    When a simple goal "Add a comment to line 42" is created
    And the orchestrator agent plans this goal
    Then a minimal set of WorkNodes should be created
    And the nodes should still be properly structured
    And the planning should complete quickly
