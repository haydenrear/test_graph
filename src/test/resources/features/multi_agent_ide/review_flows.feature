@multi_agent_ide @review @workflows
Feature: Review Flows and Approval Workflows
  As the orchestration system
  I want to support human and agent review nodes
  So that work can be validated before merging and users can provide feedback

  Background:
    Given the multi-agent-ide test environment is initialized with:
      | parameter                   | value           |
      | dockerComposePath           | <composePath>   |
      | serviceToStart              | multi-agent-ide |
      | eventSubscriptionType       | websocket       |
      | testEventListenerSubscribed | all_events      |

  @review @human_review
  Scenario: HumanReviewNode is created when work requires approval
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt              |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build API endpoints |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile                 |
      | NODE_ADDED          | WORK         | work-a-node-added.json      |
      | NODE_STATUS_CHANGED | WORK         | work-a-waiting-review.json  |
      | NODE_ADDED          | HUMAN_REVIEW | human-review-added.json     |
      | NODE_STATUS_CHANGED | HUMAN_REVIEW | human-review-ready.json     |
      | NODE_STREAM_DELTA   | HUMAN_REVIEW | review-summary-delta-1.json |
    And the mock response file ""
    When a review is requested for work-A
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And a HumanReviewNode should be created as child of work-A
    And the work node status should transition to WAITING_REVIEW
    And the review node should be ready for human input
    And the review summary should include work details

  @review @agent_review
  Scenario: AgentReviewNode performs automated review of work
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                       |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Implement payment processing |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile                  |
      | NODE_ADDED          | WORK         | work-b-node-added.json       |
      | NODE_STATUS_CHANGED | WORK         | work-b-waiting-review.json   |
      | NODE_ADDED          | AGENT_REVIEW | agent-review-added.json      |
      | NODE_STATUS_CHANGED | AGENT_REVIEW | agent-review-running.json    |
      | NODE_STREAM_DELTA   | AGENT_REVIEW | review-analysis-delta-1.json |
      | NODE_STREAM_DELTA   | AGENT_REVIEW | review-analysis-delta-2.json |
      | NODE_STATUS_CHANGED | AGENT_REVIEW | agent-review-completed.json  |
    And the mock response file ""
    When the AgentReviewGraphAgent executes
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And an AgentReviewNode should be created as child of work-B
    And the review should analyze code quality and correctness
    And the review should check against the original goal
    And the review should provide detailed feedback
    And the review should result in approval or rejection

  @review @spec_summary
  Scenario: Review includes spec summary to avoid context overload
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build database layer |
    And the expected events for this scenario are:
      | eventType              | nodeType     | payloadFile                  |
      | NODE_ADDED             | WORK         | work-c-node-added.json       |
      | NODE_ADDED             | AGENT_REVIEW | agent-review-added.json      |
      | NODE_STATUS_CHANGED    | AGENT_REVIEW | agent-review-running.json    |
      | SPEC_SECTION_RETRIEVED | WORK         | spec-summary-for-review.json |
      | NODE_STREAM_DELTA      | AGENT_REVIEW | review-spec-summary-1.json   |
    And the mock response file ""
    When the review node starts execution
    Then the expected events should have been received
    And a WorkNode should be created
    And an AgentReviewNode should be created as child of work-C
    And the spec summary should be retrieved instead of full spec
    And the review should include spec summary in its analysis
    And LLM context should be managed efficiently

  @review @spec_sections
  Scenario: Review reads specific spec sections for detailed analysis
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                    |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Integrate payment gateway |
    And the expected events for this scenario are:
      | eventType              | nodeType     | payloadFile                         |
      | NODE_ADDED             | WORK         | work-d-node-added.json              |
      | NODE_ADDED             | AGENT_REVIEW | agent-review-added.json             |
      | NODE_STATUS_CHANGED    | AGENT_REVIEW | agent-review-running.json           |
      | SPEC_SECTION_RETRIEVED | WORK         | spec-plan-section.json              |
      | SPEC_SECTION_RETRIEVED | WORK         | spec-status-section.json            |
      | SPEC_SECTION_RETRIEVED | WORK         | spec-payment-submodule.json         |
      | NODE_STREAM_DELTA      | AGENT_REVIEW | review-plan-check-delta-1.json      |
      | NODE_STREAM_DELTA      | AGENT_REVIEW | review-submodule-check-delta-1.json |
    And the mock response file ""
    When the review reads spec sections
    Then the expected events should have been received
    And a WorkNode should be created
    And an AgentReviewNode should be created as child of work-D
    And Plan section should be retrieved for review
    And Status section should be retrieved for review
    And Submodules sections should be retrieved for review
    And review should verify work against plan

  @review @submodule_review
  Scenario: Review includes submodule-related changes in its analysis
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                          |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Integrate auth and payment APIs |
    And the expected events for this scenario are:
      | eventType              | nodeType     | payloadFile                 |
      | NODE_ADDED             | WORK         | work-e-node-added.json      |
      | NODE_ADDED             | AGENT_REVIEW | agent-review-added.json     |
      | NODE_STATUS_CHANGED    | AGENT_REVIEW | agent-review-running.json   |
      | SPEC_SECTION_RETRIEVED | WORK         | spec-auth-submodule.json    |
      | SPEC_SECTION_RETRIEVED | WORK         | spec-payment-submodule.json |
      | NODE_STREAM_DELTA      | AGENT_REVIEW | review-integration-1.json   |
      | NODE_STATUS_CHANGED    | AGENT_REVIEW | review-approved.json        |
    And the mock response file ""
    When the review analyzes multi-submodule work
    Then the expected events should have been received
    And a WorkNode should be created
    And an AgentReviewNode should be created as child of work-E
    And auth-api submodule changes should be reviewed
    And payment-api submodule changes should be reviewed
    And integration between submodules should be verified
    And review should approve if all submodules are correct

  @review @approval
  Scenario: Work node transitions to COMPLETED when review is approved
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build search service |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile             |
      | NODE_ADDED          | WORK         | work-f-node-added.json  |
      | NODE_ADDED          | HUMAN_REVIEW | human-review-added.json |
      | NODE_STATUS_CHANGED | HUMAN_REVIEW | review-approved.json    |
      | NODE_STATUS_CHANGED | WORK         | work-f-completed.json   |
    And the mock response file ""
    When the HumanReviewNode approves the work
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And a HumanReviewNode should be created as child of work-F
    And the review node status should become COMPLETED
    And the work node status should transition to COMPLETED
    And the work can proceed to merging

  @review @rejection
  Scenario: Work node transitions to WAITING_INPUT when review requests changes
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                     |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build authentication layer |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile                  |
      | NODE_ADDED          | WORK         | work-g-node-added.json       |
      | NODE_ADDED          | HUMAN_REVIEW | human-review-added.json      |
      | NODE_STATUS_CHANGED | HUMAN_REVIEW | review-revisions-needed.json |
      | NODE_STATUS_CHANGED | WORK         | work-g-waiting-input.json    |
    And the mock response file ""
    When the HumanReviewNode requests revisions
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And a HumanReviewNode should be created as child of work-G
    And the review node should include revision feedback
    And the work node should transition to WAITING_INPUT
    And the agent can resume work based on feedback

  @review @feedback_capture
  Scenario: Review feedback is captured for agent follow-up
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt            |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Add caching layer |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile               |
      | NODE_ADDED          | WORK         | work-h-node-added.json    |
      | NODE_ADDED          | HUMAN_REVIEW | human-review-added.json   |
      | NODE_STATUS_CHANGED | HUMAN_REVIEW | review-with-feedback.json |
      | NODE_STATUS_CHANGED | WORK         | work-h-waiting-input.json |
    And the mock response file ""
    When the HumanReviewNode provides revision feedback
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And a HumanReviewNode should be created as child of work-H
    And the feedback should be included in the review node
    And the work node should store the feedback
    And the feedback should be used to guide next editing iteration

  @review @multiple_reviewers
  Scenario: Multiple reviewers provide independent reviews
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                        |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build critical infrastructure |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile                 |
      | NODE_ADDED          | WORK         | work-i-node-added.json      |
      | NODE_ADDED          | AGENT_REVIEW | agent-review-added.json     |
      | NODE_STATUS_CHANGED | AGENT_REVIEW | agent-review-completed.json |
      | NODE_ADDED          | HUMAN_REVIEW | human-review-added.json     |
      | NODE_STATUS_CHANGED | HUMAN_REVIEW | human-review-ready.json     |
    And the mock response file ""
    When multiple reviewers provide independent reviews
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And an AgentReviewNode should be created as child of work-I
    And a HumanReviewNode should be created as child of work-I
    And agent review should complete first
    And human review should then proceed
    And both reviews should be documented
    And consensus should determine final approval

  @review @timeout
  Scenario: Review node timeout transitions to waiting or failure state
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                    |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build notification system |
    And the review timeout is set to 5 minutes
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile             |
      | NODE_ADDED          | WORK         | work-j-node-added.json  |
      | NODE_ADDED          | HUMAN_REVIEW | human-review-added.json |
      | NODE_STATUS_CHANGED | HUMAN_REVIEW | review-timeout.json     |
      | NODE_STATUS_CHANGED | WORK         | work-j-failed.json      |
    And the mock response file ""
    When review timeout is reached without approval
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And a HumanReviewNode should be created as child of work-J
    And the review should timeout gracefully
    And the work should transition to FAILED or WAITING_INPUT
    And the system should notify about timeout

  @review @content_types
  Scenario: Reviews handle different content types appropriately
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt               |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build API with tests |
    And the expected events for this scenario are:
      | eventType           | nodeType     | payloadFile               |
      | NODE_ADDED          | WORK         | work-k-node-added.json    |
      | NODE_ADDED          | AGENT_REVIEW | agent-review-added.json   |
      | NODE_STREAM_DELTA   | AGENT_REVIEW | code-quality-review.json  |
      | NODE_STREAM_DELTA   | AGENT_REVIEW | test-coverage-review.json |
      | NODE_STATUS_CHANGED | AGENT_REVIEW | review-completed.json     |
    And the mock response file ""
    When review analyzes code, tests, and documentation
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And an AgentReviewNode should be created as child of work-K
    And code quality should be evaluated
    And test coverage should be analyzed
    And documentation completeness should be checked

  @review @spec_update
  Scenario: Review completion updates spec with feedback and approval status
    Given a computation graph with the following structure:
      | nodeId | nodeType     | status | parentId | children | prompt                 |
      | orch-1 | ORCHESTRATOR | READY  | null     | null     | Build monitoring stack |
    And the expected events for this scenario are:
      | eventType            | nodeType     | payloadFile             |
      | NODE_ADDED           | WORK         | work-l-node-added.json  |
      | NODE_ADDED           | AGENT_REVIEW | agent-review-added.json |
      | NODE_STATUS_CHANGED  | AGENT_REVIEW | review-approved.json    |
      | SPEC_SECTION_UPDATED | WORK         | spec-review-status.json |
    And the mock response file ""
    When review completes with approval
    Then the expected events should have been received
    And a WorkNode should be created as child of OrchestratorNode
    And an AgentReviewNode should be created as child of work-L
    And the spec Review section should be created or updated
    And the approval status should be documented
    And review feedback should be included in spec
    And spec should remain valid after review
