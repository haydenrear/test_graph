@multi_agent_ide @editing @streaming
Feature: Editing Code and Streaming Output
  As the orchestration system
  I want editor agents to stream code changes in real-time
  So that users see progress as code is being generated and can integrate streaming into the UI

  Background:
    Given the multi-agent-ide test environment is initialized with:
      | parameter                   | value           |
      | dockerComposePath           | <composePath>   |
      | serviceToStart              | multi-agent-ide |
      | eventSubscriptionType       | websocket       |
      | testEventListenerSubscribed | all_events      |

  @editing @main_repo_only
  Scenario: EditorGraphAgent edits code in main repository only
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                                  |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Implement user authentication endpoints |
    And the expected events for this scenario are:
      | eventType           | nodeType | payloadFile            |
      | NODE_ADDED          | WORK     | work-a-node-added.json |
      | NODE_STATUS_CHANGED | WORK     | work-a-running.json    |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-1.json    |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-2.json    |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-3.json    |
      | NODE_STATUS_CHANGED | WORK     | work-a-completed.json  |
    And the mock response file ""
    When the EditorGraphAgent executes on work-A
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And code changes should be made to files in the main worktree
    And no changes should occur in submodule worktrees
    And the spec Plan section should be updated with the implementation details

  @editing @submodule_only
  Scenario: EditorGraphAgent edits code in a specific submodule
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                                      |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Implement payment processing in payment-api |
    And the expected events for this scenario are:
      | eventType           | nodeType | payloadFile            |
      | NODE_ADDED          | WORK     | work-b-node-added.json |
      | NODE_STATUS_CHANGED | WORK     | work-b-running.json    |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-1.json    |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-2.json    |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-3.json    |
      | NODE_STATUS_CHANGED | WORK     | work-b-completed.json  |
    And the mock response file ""
    When the EditorGraphAgent executes on work-B targeting payment-api submodule
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And code changes should be made to files in the payment-api submodule worktree
    And the main repository should remain unmodified
    And the submodule worktree should track modifications

  @editing @main_and_submodules
  Scenario: EditorGraphAgent edits both main repo and submodules in same execution
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                                               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Integrate auth-api and payment-api with main service |
    And the expected events for this scenario are:
      | eventType           | nodeType | payloadFile                     |
      | NODE_ADDED          | WORK     | work-c-node-added.json          |
      | NODE_STATUS_CHANGED | WORK     | work-c-running.json             |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-auth-1.json        |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-main-1.json        |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-payment-1.json     |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-integration-1.json |
      | NODE_STATUS_CHANGED | WORK     | work-c-completed.json           |
    And the mock response file ""
    When the EditorGraphAgent executes on work-C with mixed main and submodule edits
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And changes should be made to the main worktree
    And changes should be made to the auth-api submodule worktree
    And changes should be made to the payment-api submodule worktree
    And all changes should be tracked in their respective worktrees

  @streaming @basic
  Scenario: Code streaming produces token stream via NODE_STREAM_DELTA events
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                  |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Write REST API endpoint |
    And the expected events for this scenario are:
      | eventType           | nodeType | payloadFile            |
      | NODE_ADDED          | WORK     | work-d-node-added.json |
      | NODE_STATUS_CHANGED | WORK     | work-d-running.json    |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-1.json    |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-2.json    |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-3.json    |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-4.json    |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-5.json    |
      | NODE_STATUS_CHANGED | WORK     | work-d-completed.json  |
    And the mock response file ""
    When the EditorGraphAgent streams code generation
    Then the expected events should have been received
    And a WorkNode should be created
    And each NODE_STREAM_DELTA event should contain a token increment
    And tokens should arrive in order
    And the total streamed content should form complete, valid code

  @streaming @token_count
  Scenario: Streaming completes exactly at expected token count
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                    |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Implement data validation |
    And the EditorGraphAgent is configured to generate 250 tokens
    And the expected events for this scenario are:
      | eventType           | nodeType | payloadFile               |
      | NODE_ADDED          | WORK     | work-e-node-added.json    |
      | NODE_STATUS_CHANGED | WORK     | work-e-running.json       |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-batch-1.json |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-batch-2.json |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-batch-3.json |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-batch-4.json |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-batch-5.json |
      | NODE_STATUS_CHANGED | WORK     | work-e-completed.json     |
    And the mock response file ""
    When the EditorGraphAgent streams 250 tokens
    Then the expected events should have been received
    And a WorkNode should be created
    And a total of 250 tokens should have been streamed
    And the last NODE_STREAM_DELTA should mark end-of-stream
    And the node status should transition to COMPLETED after streaming completes

  @streaming @listener_reception
  Scenario: Test listener receives all streamed tokens in order
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt          |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Write unit test |
    And the EditorGraphAgent is configured to generate 100 tokens
    And the expected events for this scenario are:
      | eventType         | nodeType | payloadFile            |
      | NODE_ADDED        | WORK     | work-f-node-added.json |
      | NODE_STREAM_DELTA | WORK     | stream-delta-1.json    |
      | NODE_STREAM_DELTA | WORK     | stream-delta-2.json    |
      | NODE_STREAM_DELTA | WORK     | stream-delta-3.json    |
    And the mock response file ""
    When the EditorGraphAgent streams tokens
    Then the test listener should have received all streaming tokens
    And a WorkNode should be created
    And tokens should be received in exact order
    And no tokens should be dropped or duplicated

  @spec_updates @plan_section
  Scenario: Spec Plan section is updated as editing progresses
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt            |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Add caching layer |
    And the expected events for this scenario are:
      | eventType            | nodeType | payloadFile            |
      | NODE_ADDED           | WORK     | work-g-node-added.json |
      | NODE_STATUS_CHANGED  | WORK     | work-g-running.json    |
      | NODE_STREAM_DELTA    | WORK     | stream-delta-1.json    |
      | NODE_STREAM_DELTA    | WORK     | stream-delta-2.json    |
      | SPEC_SECTION_UPDATED | WORK     | spec-plan-updated.json |
      | NODE_STATUS_CHANGED  | WORK     | work-g-completed.json  |
    And the mock response file ""
    When the EditorGraphAgent executes
    Then the expected events should have been received
    And a WorkNode should be created
    And the spec Plan section should include implementation details
    And the spec Plan should reference files being modified
    And the spec Plan should document the approach taken

  @spec_updates @status_section
  Scenario: Spec Status section reflects editing progress
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                   |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Implement error handling |
    And the expected events for this scenario are:
      | eventType            | nodeType | payloadFile              |
      | NODE_ADDED           | WORK     | work-h-node-added.json   |
      | NODE_STATUS_CHANGED  | WORK     | work-h-running.json      |
      | NODE_STREAM_DELTA    | WORK     | stream-delta-1.json      |
      | NODE_STREAM_DELTA    | WORK     | stream-delta-2.json      |
      | SPEC_SECTION_UPDATED | WORK     | spec-status-updated.json |
      | NODE_STATUS_CHANGED  | WORK     | work-h-completed.json    |
    And the mock response file ""
    When the EditorGraphAgent executes
    Then the expected events should have been received
    And a WorkNode should be created
    And the spec Status section should indicate in-progress status during execution
    And the spec Status should update completion percentage
    And the spec Status should transition to complete when done

  @spec_updates @submodule_sections
  Scenario: Spec submodule sections track changes per submodule
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                                    |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Integrate payment and auth APIs with main |
    And the expected events for this scenario are:
      | eventType            | nodeType | payloadFile                         |
      | NODE_ADDED           | WORK     | work-i-node-added.json              |
      | NODE_STATUS_CHANGED  | WORK     | work-i-running.json                 |
      | NODE_STREAM_DELTA    | WORK     | stream-delta-1.json                 |
      | NODE_STREAM_DELTA    | WORK     | stream-delta-2.json                 |
      | SPEC_SECTION_UPDATED | WORK     | spec-auth-submodule-updated.json    |
      | SPEC_SECTION_UPDATED | WORK     | spec-payment-submodule-updated.json |
      | NODE_STATUS_CHANGED  | WORK     | work-i-completed.json               |
    And the mock response file ""
    When the EditorGraphAgent edits both main and submodules
    Then the expected events should have been received
    And a WorkNode should be created
    And the spec Submodules/auth-api section should be updated
    And the spec Submodules/payment-api section should be updated
    And each submodule section should document its specific changes

  @streaming @varying_patterns
  Scenario: Streaming handles varying token rates
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                  |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Complex feature request |
    And the EditorGraphAgent is configured with varying token emission patterns
    And the expected events for this scenario are:
      | eventType           | nodeType | payloadFile               |
      | NODE_ADDED          | WORK     | work-j-node-added.json    |
      | NODE_STATUS_CHANGED | WORK     | work-j-running.json       |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-burst-1.json |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-burst-2.json |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-slow-1.json  |
      | NODE_STREAM_DELTA   | WORK     | stream-delta-burst-3.json |
      | NODE_STATUS_CHANGED | WORK     | work-j-completed.json     |
    And the mock response file ""
    When the EditorGraphAgent streams with variable rates
    Then the expected events should have been received
    And a WorkNode should be created
    And all tokens should be captured regardless of emission rate
    And test listener should handle variable latency between events
    And streaming should complete successfully

  @parallel_editing @independent_nodes
  Scenario: Multiple WorkNodes can edit in parallel without interference
    Given multiple computation graphs with the following structure:
      | graphId | nodeId | nodeType     | status | parentId | children | prompt                         |
      | task-A  | orch-A | ORCHESTRATOR | READY  | null     | null     | Add logging to auth service    |
      | task-B  | orch-B | ORCHESTRATOR | READY  | null     | null     | Add metrics to payment service |
    And the expected events for this scenario are:
      | eventType         | nodeType | payloadFile            |
      | NODE_ADDED        | WORK     | work-a-node-added.json |
      | NODE_STREAM_DELTA | WORK     | stream-delta-a-1.json  |
      | NODE_ADDED        | WORK     | work-b-node-added.json |
      | NODE_STREAM_DELTA | WORK     | stream-delta-b-1.json  |
      | NODE_STREAM_DELTA | WORK     | stream-delta-a-2.json  |
      | NODE_STREAM_DELTA | WORK     | stream-delta-b-2.json  |
    And the mock response file ""
    When both EditorGraphAgents execute in parallel
    Then the expected events should have been received
    And WorkNodes should be created on their respective orchestrators
    And work-A should modify auth-api submodule only
    And work-B should modify payment-api submodule only
    And modifications should not interfere with each other
    And both nodes should complete successfully
