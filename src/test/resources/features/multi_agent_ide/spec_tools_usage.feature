@multi_agent_ide @spec_tools @specs
Feature: Spec File Tools and Manipulation
  As agents and users
  I want to query and manipulate spec files without loading entire documents into context
  So that large specs don't blow LLM context and agents work efficiently with targeted spec sections

  Background:
    Given the multi-agent-ide test environment is initialized with:
      | parameter                   | value           |
      | dockerComposePath           | <composePath>   |
      | serviceToStart              | multi-agent-ide |
      | eventSubscriptionType       | websocket       |
      | testEventListenerSubscribed | all_events      |

  @spec_tools @validation
  Scenario: Spec validation succeeds for well-formed specs
    Given a computation graph with the following structure:
      | nodeId | nodeType | status    | parentId | children | prompt              |
      | work-A | WORK     | COMPLETED | null     | null     | Build API endpoints |
    And the expected events for this scenario are:
    And the mock response file ""
    When spec validation is performed on work-A spec
    Then the expected events should have been received
    And the spec should be marked as valid
    And validation should confirm all required sections exist
    And validation should confirm proper markdown structure

  @spec_tools @validation_failure
  Scenario: Spec validation fails for malformed specs
    Given a computation graph with the following structure:
      | nodeId | nodeType | status    | parentId | children | prompt              |
      | work-B | WORK     | COMPLETED | null     | null     | Build API endpoints |
    And the expected events for this scenario are:
    And the mock response file ""
    When spec validation is performed on malformed spec
    Then the expected events should have been received
    And validation should fail with clear error message
    And the error should indicate missing sections
    And spec operations should not proceed until fixed

  @spec_tools @summary
  Scenario: get_summary returns concise overview without full content
    Given a computation graph with the following structure:
      | nodeId | nodeType | status    | parentId | children | prompt              |
      | work-C | WORK     | COMPLETED | null     | null     | Build API endpoints |
    And the expected events for this scenario are:
    And the mock response file ""
    When get_summary is called on work-C spec
    Then the expected events should have been received
    And a summary should be returned without loading full spec
    And the summary should include section headers and paths
    And the summary should indicate spec status and completion percentage
    And the summary should list all subsections (Plan, Status, Submodules, etc.)

  @spec_tools @summary_sections
  Scenario: Summary includes section paths for navigation
    Given a computation graph with the following structure:
      | nodeId | nodeType | status    | parentId | children | prompt              |
      | work-D | WORK     | COMPLETED | null     | null     | Build API endpoints |
    And the expected events for this scenario are:
    And the mock response file ""
    When get_summary is called on spec with submodules
    Then the expected events should have been received
    And the summary should include path "#/Header"
    And the summary should include path "#/Plan"
    And the summary should include path "#/Status"
    And the summary should include path "#/Submodules/auth-api"
    And the summary should include path "#/Submodules/payment-api"

  @spec_tools @get_section
  Scenario: get_section retrieves single section without loading entire spec
    Given a computation graph with the following structure:
      | nodeId | nodeType | status    | parentId | children | prompt              |
      | work-E | WORK     | COMPLETED | null     | null     | Build API endpoints |
    And the expected events for this scenario are:
    And the mock response file ""
    When get_section is called with headerPath "#/Plan"
    Then the expected events should have been received
    And only the Plan section should be returned
    And the Plan section should be well-formed markdown
    And the entire spec should not be loaded into context
    And the section should be ready for agent processing

  @spec_tools @get_submodule_section
  Scenario: get_section retrieves submodule-specific sections
    Given a computation graph with the following structure:
      | nodeId | nodeType | status    | parentId | children | prompt              |
      | work-F | WORK     | COMPLETED | null     | null     | Build API endpoints |
    And the expected events for this scenario are:
    And the mock response file ""
    When get_section is called with headerPath "#/Submodules/auth-api"
    Then the expected events should have been received
    And only the auth-api submodule section should be returned
    And the section should include Plan subsection for that submodule
    And the section should include Status subsection for that submodule
    And changes and requirements specific to auth-api should be included

  @spec_tools @status_retrieval
  Scenario: Agents use get_section to check Status without context overhead
    Given a computation graph with the following structure:
      | nodeId | nodeType | status  | parentId | children | prompt              |
      | work-G | WORK     | RUNNING | null     | null     | Build API endpoints |
    And the expected events for this scenario are:
    And the mock response file ""
    When the EditorGraphAgent calls get_section("#/Status")
    Then the expected events should have been received
    And the Status section should be returned quickly
    And the agent can determine progress without loading full spec
    And the agent can decide next steps based on status

  @spec_tools @plan_retrieval
  Scenario: Agents use get_section to access Plan details
    Given a computation graph with the following structure:
      | nodeId | nodeType | status  | parentId | children | prompt              |
      | work-H | WORK     | RUNNING | null     | null     | Build API endpoints |
    And the expected events for this scenario are:
    And the mock response file ""
    When the EditorGraphAgent calls get_section("#/Plan")
    Then the expected events should have been received
    And the Plan section should be returned
    And the Plan should contain detailed implementation steps
    And the agent can follow the plan step-by-step
    And submodule-specific plan items should be clearly marked

  @spec_tools @update_pattern
  Scenario: Spec updates use targeted section replacement
    Given a computation graph with the following structure:
      | nodeId | nodeType | status  | parentId | children | prompt              |
      | work-I | WORK     | RUNNING | null     | null     | Build API endpoints |
    And the expected events for this scenario are:
    And the mock response file ""
    When the EditorGraphAgent updates the Plan section
    Then the expected events should have been received
    And only the Plan section should be modified
    And other sections should remain unchanged
    And the spec should be valid after update
    And the update should be atomic and consistent

  @spec_tools @concurrent_reads
  Scenario: Multiple agents can read different spec sections concurrently
    Given multiple computation graphs with the following structure:
      | graphId | nodeId | nodeType | status  | parentId | children | prompt              |
      | task-A  | work-A | WORK     | RUNNING | null     | null     | Implement feature A |
      | task-B  | work-B | WORK     | RUNNING | null     | null     | Implement feature B |
    And the expected events for this scenario are:
    And the mock response file ""
    When both agents call get_section on their respective specs
    Then the expected events should have been received
    And work-A Plan section should be retrieved independently
    And work-B Plan section should be retrieved independently
    And concurrent reads should not interfere
    And both agents should proceed with their work

  @spec_tools @merge_scenario
  Scenario: Spec merge uses get_summary and get_section to integrate child into parent
    Given a computation graph with the following structure:
      | nodeId | nodeType | status    | parentId | children | prompt      |
      | work-P | WORK     | COMPLETED | null     | null     | Parent work |
      | work-C | WORK     | COMPLETED | work-P   | null     | Child work  |
    And the expected events for this scenario are:
