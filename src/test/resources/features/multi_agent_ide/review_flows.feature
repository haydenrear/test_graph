@multi_agent_ide @review
Feature: Human and Agent Review Flows
  As a review orchestrator
  I want to insert review nodes into the computation graph
  So that work can be approved, rejected, or sent back for revision

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events

  @review @core
  Scenario: Reviewable work node triggers review request event
    Given a WorkNode has completed streaming and is in WAITING_REVIEW state
    And the node is marked as Reviewable
    When the execution engine detects this state
    Then a NodeReviewRequestedEvent should be emitted
    And a HumanReviewNode should be created as a child of the WorkNode
    And the HumanReviewNode should be in WAITING_INPUT state

  @review @core
  Scenario: Human review node awaits user approval via message
    Given a HumanReviewNode exists in WAITING_INPUT state
    And it contains the output from the WorkNode for human evaluation
    When the test sends an approval message
    Then the HumanReviewNode should transition to COMPLETED
    And the parent WorkNode should transition to COMPLETED
    And NodeStatusChangedEvent should be emitted for both nodes

  @review @rejection
  Scenario: Rejection of review creates new work node for revision
    Given a HumanReviewNode in WAITING_INPUT state with work output
    When a rejection message is sent with feedback "Needs more error handling"
    Then the HumanReviewNode should transition to COMPLETED with rejection status
    And a new WorkNode should be created as a sibling
    And the new node should have status READY for re-execution
    And the feedback should be attached as an annotation

  @review @agent_review
  Scenario: Agent review node can also be created for internal validation
    Given a WorkNode has completed
    And agent review is enabled for this task type
    When the execution engine checks if review is needed
    Then an AgentReviewNode should be created
    And the AgentReviewGraphAgent should execute automatically
    And the agent should analyze the work without human intervention

  @review @approval
  Scenario Outline: Different review paths based on content type
    Given a WorkNode with content type "<contentType>" is in WAITING_REVIEW
    When review nodes are created
    Then the review handler should be "<reviewHandler>"
    And the approval criteria should match "<criteria>"

    Examples:
      | contentType    | reviewHandler   | criteria                |
      | code           | HumanReviewNode | code_quality_standards  |
      | documentation  | HumanReviewNode | clarity_completeness    |
      | test_cases     | AgentReviewNode | coverage_quality        |
      | architecture   | HumanReviewNode | design_principles       |

  @review @cascade
  Scenario: Review approval triggers downstream node execution
    Given WorkNode A is approved and transitions to COMPLETED
    And WorkNode B is dependent on WorkNode A
    And WorkNode B is in PENDING state
    When the completion event is processed
    Then WorkNode B should transition to READY
    And the execution engine should schedule B for execution
    And NodeStatusChangedEvent should be emitted for WorkNode B

  @review @branching
  Scenario: Reviewer can request changes by branching during review
    Given a HumanReviewNode in WAITING_INPUT state
    When the reviewer sends a branch request with modified goal
    Then a new WorkNode should be created as a branch
    And the parent WorkNode should remain WAITING_REVIEW
    And the branch node should have status READY
    And both the original and branch should be tracked in the graph

  @review @annotations
  Scenario: Review process supports annotations and comments
    Given a HumanReviewNode in WAITING_INPUT state
    When a reviewer adds annotations via message
    Then the annotations should be stored with the node
    And the parent WorkNode should also receive annotation updates
    And annotations should be retrievable and displayable

  @review @multiple_reviewers
  Scenario: Multiple reviewers can sequentially review work
    Given a WorkNode requires review from 2 reviewers
    When the first reviewer approves via message
    Then a second HumanReviewNode should be created
    And the first approval should be recorded
    And the second reviewer should receive the work for review
    And final approval only occurs after both have approved

  @review @timeout
  Scenario: Review nodes can have timeout behavior
    Given a HumanReviewNode in WAITING_INPUT state with 5 minute timeout
    When 5 minutes elapse without approval
    Then the node should transition to TIMEOUT state
    And a notification should be sent
    And the orchestrator should handle escalation (send reminder or reassign)

  @review @persistence
  Scenario: Review decisions are persisted with full audit trail
    Given a review process completes with approval
    When the HumanReviewNode and approval status are stored
    Then the database should contain the review node
    And the approval timestamp should be recorded
    And the reviewer identity should be recorded
    And the complete audit trail should be queryable
