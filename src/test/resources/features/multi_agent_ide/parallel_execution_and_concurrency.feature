@multi_agent_ide @parallelism @concurrency
Feature: Parallel Execution and Concurrent Agent Operations
  As the execution engine
  I want to safely execute multiple nodes in parallel
  So that independent work items can be processed simultaneously for efficiency

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events
    And the execution engine is configured for parallel execution

  @parallelism @core
  Scenario: Multiple independent nodes execute concurrently
    Given a goal is planned creating 3 independent WorkNodes
    And all 3 nodes are in READY state
    And there are no dependencies between them
    When the execution engine steps through all nodes
    Then all 3 nodes should transition to RUNNING simultaneously
    And NodeStatusChangedEvent should be received for all 3 nodes
    And all 3 should stream output concurrently
    And the test listener should correctly attribute all events to their nodes

  @parallelism @dependency_ordering
  Scenario: Dependent nodes respect execution ordering
    Given WorkNode A, B, and C where B depends on A and C depends on B
    And all are in READY state
    When the execution engine begins
    Then A should transition to RUNNING first
    And B should remain READY until A completes
    And C should remain READY until B completes
    And the execution order should be strictly A → B → C

  @parallelism @mixed_dependencies
    Scenario: Complex dependency graph with parallel and sequential work
    Given a graph with nodes: A (root), B and C (depend on A), D (depends on B and C)
    And A is READY, others are PENDING
    When execution begins
    Then A should execute first
    Then B and C should execute in parallel after A completes
    Then D should execute only after both B and C complete
    And all events should maintain correct node attribution

  @parallelism @resource_limits
  Scenario: Execution respects concurrent worker limits
    Given the system is configured with max 2 concurrent executions
    And 5 independent WorkNodes are in READY state
    When execution begins
    Then at most 2 nodes should be RUNNING at any time
    And when a node completes, the next READY node should start
    And all 5 nodes should eventually complete
    And execution should respect the concurrency limit throughout

  @parallelism @streaming_multiplexing
  Scenario: Streaming events from multiple nodes are correctly multiplexed
    Given 3 WorkNodes executing in parallel
    And each is streaming output with different pacing
    When tokens are emitted from all 3 nodes
    Then NodeStreamDeltaEvent should correctly attribute each token to its node
    And the test listener should receive interleaved tokens from all 3
    And the token sequence number should allow reconstruction per-node
    And no token should be attributed to the wrong node

  @parallelism @state_consistency
  Scenario: Parallel execution maintains consistent graph state
    Given multiple nodes executing in parallel
    And all are modifying shared execution context
    When concurrent updates occur
    Then the graph state should remain consistent
    And no race conditions should occur in state updates
    And NodeStatusChangedEvent ordering should be preserved
    And the database should reflect all updates correctly

  @parallelism @interruption_handling
  Scenario: Interrupting one parallel node doesn't affect others
    Given 3 WorkNodes executing in parallel
    And all are RUNNING
    When an interrupt message is sent for node A only
    Then node A should transition to WAITING_INPUT
    And nodes B and C should continue RUNNING unaffected
    And NodeStatusChangedEvent should only mention node A
    And the execution of B and C should not be disrupted

  @parallelism @branch_parallelism
  Scenario: Branches can execute in parallel with original path
    Given a WorkNode in execution
    When a branch is requested with modified goal
    And the original continues executing
    Then both original and branch should be able to RUNNING in parallel
    And both should emit their own streaming events
    And the system should track both paths independently
    And results from both should be available for merge

  @parallelism @agent_concurrency
  Scenario: Multiple agents can operate on different nodes simultaneously
    Given 3 WorkNodes in RUNNING state
    And each is assigned to a different GraphAgent type
    When all execute simultaneously
    Then each agent should execute independently
    And there should be no lock contention between agents
    And each agent should access its node's data safely
    And all outputs should be persisted correctly

  @parallelism @event_ordering_concurrent
  Scenario: Event bus maintains order within a node despite parallelism
    Given a WorkNode streaming output with 100 tokens
    And other nodes are executing in parallel
    When all nodes stream tokens concurrently
    Then the 100 tokens from the focused node should arrive in correct order
    And the sequence numbers for that node should be sequential
    And parallel tokens from other nodes should not interleave within this node's sequence
    And the test listener should be able to reconstruct the complete output

  @concurrency @deadlock_prevention
  Scenario: System prevents deadlocks in parallel graph execution
    Given a complex graph with multiple interdependencies
    When nodes execute in parallel with complex dependencies
    Then no deadlock should occur
    And all nodes should eventually reach completion
    And if a node becomes stuck, timeout should detect it
    And the system should recover gracefully from any potential deadlock

  @parallelism @performance_scaling
  Scenario: Performance scales with number of parallel nodes
    Given the system configured with 1 concurrent execution
    When a goal with 10 nodes is executed sequentially
    Then total execution time should be T seconds
    And when the system is reconfigured to 5 concurrent executions
    And the same goal is executed with parallelism enabled
    Then total execution time should be approximately T/5 seconds
    And performance improvement should be measurable

  @parallelism @merge_concurrent_results
  Scenario: Merge operation works with concurrently executing branches
    Given two branches created from a common node
    And both branches are executing in parallel
    And branch A completes before branch B
    When a merge is requested
    Then the merge should wait for both branches to complete
    And NodeStatusChangedEvent should be received for each completion
    And the merge should aggregate both completed results
    And the merge output should combine both results correctly
