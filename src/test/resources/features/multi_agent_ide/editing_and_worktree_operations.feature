@multi_agent_ide @editing @worktrees @streaming
Feature: Editing and Worktree Operations with Streaming
  As an editor agent
  I want to generate code changes inside worktrees and stream the output
  So that users see edits being generated in real-time and can review them before merge

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events
    And LangChain4j streaming models are configured
    And git is properly configured in the container

  @editing @streaming @core
  Scenario: EditorGraphAgent streams code generation to worktree
    Given a WorkNode in RUNNING state with associated worktree
    And a prompt "Add error handling to auth service"
    When the EditorGraphAgent begins execution with streaming
    Then NodeStreamDeltaEvent should be emitted with code tokens
    And each token should contribute to code being generated
    And the test listener should receive tokens incrementally
    And after streaming completes, the generated code should exist in the worktree

  @editing @worktree_write
  Scenario: Streamed output is committed to worktree files
    Given a WorkNode streaming code generation
    And the stream contains complete method implementations
    When streaming completes
    Then the code should be written to appropriate files in the worktree
    And the worktree should show modified files via git status
    And the changes should be staged or committed as appropriate
    And the node payload should contain a reference to the diff

  @editing @diff_generation
  Scenario: Diffs are generated and stored with node
    Given a WorkNode has completed code generation in its worktree
    And the worktree contains file modifications
    When a diff operation is triggered
    Then a unified diff should be generated comparing parent worktree to current
    And the diff should be stored in NodeUpdatedEvent payload
    And the diff should show before/after for modified files
    And the diff should be queryable from the node data

  @editing @user_edit
  Scenario: User can edit node prompt before/during execution
    Given a WorkNode in READY state with original prompt
    When a user edits the prompt via message with new instructions
    Then the NodeUpdatedEvent should contain the new prompt
    And the node should remain READY (if not executing)
    And if node is RUNNING, it should transition to WAITING_INPUT
    And upon resume, execution should use the new prompt

  @editing @worktree_branch
  Scenario: Editing creates branch in worktree for safe changes
    Given a WorkNode with associated worktree on branch "main"
    When the EditorGraphAgent begins execution
    Then a new git branch should be created for the edits (e.g., "work-{nodeId}")
    And all modifications should occur on this branch
    And the parent worktree's "main" branch should remain unaffected
    And NodeUpdatedEvent should include the branch name

  @editing @concurrent_edits
  Scenario: Multiple nodes can edit in parallel without conflicts
    Given WorkNode A and WorkNode B with separate worktrees from same parent
    When both A and B stream code generation concurrently
    Then each should modify its own worktree independently
    And changes in A's worktree should not interfere with B
    And both NodeStreamDeltaEvent streams should be independent
    And both sets of changes should be preserved

  @editing @file_targeting
  Scenario: EditorGraphAgent can target specific files in worktree
    Given a WorkNode with worktree containing multiple files
    And the prompt specifies "Update authentication.py and add logging.py"
    When streaming generates changes
    Then the EditorGraphAgent should modify authentication.py
    And the EditorGraphAgent should create logging.py
    And the diff should reflect both operations
    And the streaming should indicate which file is being edited

  @editing @language_support
  Scenario Outline: Editor supports multiple programming languages
    Given a WorkNode in a worktree with <language> files
    And the prompt contains language-specific instructions
    When the EditorGraphAgent executes with streaming
    Then the generated code should be syntactically correct <language>
    And the streaming tokens should preserve code formatting
    And the final code should integrate with existing <language> files

    Examples:
      | language |
      | Python   |
      | Java     |
      | TypeScript |
      | Go       |

  @editing @partial_output
  Scenario: Partial output is recoverable if execution interrupts
    Given a WorkNode streaming code generation
    And 50% of the code has been streamed
    When an interrupt message is sent
    Then the node should transition to WAITING_INPUT
    And the partial code should be preserved in the worktree
    And the user can review the partial output
    And upon resume, execution can continue or restart

  @editing @auto_commit
  Scenario: Changes are automatically staged or committed
    Given a WorkNode completes code generation with streamed output
    When the streaming finishes
    Then the worktree should have files in modified/staged state
    And optionally, changes should be auto-committed with message like "Generated by {nodeId}"
    And the git log should show the commit if auto-committed
    And NodeUpdatedEvent should indicate commit status

  @editing @validation
  Scenario: Generated code can be validated before review
    Given a WorkNode with completed code generation
    And the worktree contains generated code
    When a validation step is triggered
    Then linting or basic syntax checks should run (mocked if needed)
    And validation results should be included in node payload
    And if validation fails, node should transition to WAITING_INPUT or FAILED
    And validation errors should be reported as annotations

  @editing @merge_conflict_preview
  Scenario: Diff preview shows potential merge conflicts
    Given a WorkNode has generated code in its worktree
    And the parent worktree also has changes that might conflict
    When a conflict detection is run
    Then conflict markers should be identified in the diff
    And NodeUpdatedEvent should include conflict regions
    And the diff should highlight conflicting lines
    And user or review node should be aware of conflicts before merge

  @editing @revert
  Scenario: Edits can be reverted if not approved
    Given a WorkNode with completed edits and diffs stored
    And the node is in WAITING_REVIEW state
    When a rejection with "revert" action is sent
    Then the worktree should be reset to parent state
    And all local changes should be discarded
    And a new WorkNode can be created for retry if needed
    And the reverted state should be recorded in events
