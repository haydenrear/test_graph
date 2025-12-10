@multi_agent_ide @initialization
Feature: Goal and Computation Graph Initialization
  As a user of the multi-agent IDE
  I want goals to initialize a computation graph with proper structure and state
  So that agents can collaborate on solving the goal

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events
    And mock LangChain4j planning response is configured
    And spec file configuration is set to standard markdown format

  @graph_initialization @core
  Scenario Outline: Creating a new goal initializes an orchestrator node
    When a new goal is created via message with description "<goalDescription>"
    Then a NodeAddedEvent should be received containing an OrchestratorNode
    And the OrchestratorNode should have status "READY"
    And the OrchestratorNode should be stored in the graph database

    Examples:
      | goalDescription                    | composePath                                                |
      | Refactor the authentication module | path/to/compose/no-model-server                           |
      | Implement caching layer            | path/to/compose/no-model-server                           |

  @graph_initialization @core
  Scenario: Goal initialization sets up proper node capabilities
    When a new goal is created via message with description "Implement feature X"
    And a NodeAddedEvent is received with the OrchestratorNode
    Then the OrchestratorNode should be Branchable
    And the OrchestratorNode should be Interruptable
    And the OrchestratorNode should be Summarizable
    And the OrchestratorNode should be Viewable

  @event_bus @messaging
  Scenario: Event is published to message bus after goal creation
    When a new goal is created via message with description "Add user authentication"
    Then a NodeAddedEvent should be published to the message bus
    And the message bus event should contain the OrchestratorNode details
    And the test listener should receive the event via websocket

  @graph_initialization
  Scenario: Graph is initialized with empty child list
    When a new goal is created via message with description "Optimize database queries"
    And a NodeAddedEvent is received with the OrchestratorNode
    Then the OrchestratorNode should have no child nodes initially
    And the OrchestratorNode should have an empty completion status

  @event_ordering @core
  Scenario: Events are published in correct order during initialization
    When a new goal is created via message with description "Build notification system"
    Then exactly one NodeAddedEvent should be received
    And the event should contain a valid OrchestratorNode with a unique ID

  @state_transitions
  Scenario: OrchestratorNode transitions from READY to RUNNING
    When a new goal is created via message with description "Write API documentation"
    And a NodeAddedEvent is received with the OrchestratorNode
    And the orchestrator begins execution via message
    Then a NodeStatusChangedEvent should be received with status "RUNNING"
    And the node ID should match the original OrchestratorNode

  @multiple_goals
  Scenario: Multiple concurrent goals maintain separate graphs
    When a goal with ID "goal-1" is created via message with description "Refactor module A"
    And a goal with ID "goal-2" is created via message with description "Refactor module B"
    Then two NodeAddedEvents should be received
    And the two OrchestratorNodes should have different IDs
    And the graph database should contain both nodes as separate entries

  @messaging_types @integration
  Scenario Outline: Goal initialization works with different subscription types
    Given the event subscription type is "<subscriptionType>"
    When a new goal is created via message with description "Test goal"
    Then a NodeAddedEvent should be received via "<subscriptionType>"
    And the event data should be correctly deserialized

    Examples:
      | subscriptionType |
      | websocket        |
      | http_polling     |
      | kafka            |
