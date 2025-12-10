@multi_agent_ide @specs @tools
Feature: Spec Files and Spec Tools for Work Description and Resumability
  As an agent and orchestrator
  I want to manage spec files that describe work, status, and submodule changes
  So that large contexts are avoided and execution can resume from specs

  Background:
    Given docker-compose is started from "<composePath>"
    And the multi-agent-ide service is running
    And the event subscription type is "websocket"
    And a test event listener is subscribed to all events

  @spec @initialization @core
  Scenario: Initial spec file is created when goal is planned
    Given a goal "Build authentication service" is created and planned
    And WorkNodes are generated for main repo and submodules (if present)
    When planning completes
    Then a spec file should be created at {worktreePath}/.multi-agent-plan.md
    And the spec should contain required sections:
      | section      | content                                |
      | Header       | Metadata: nodeId, createdAt, updated   |
      | Plan         | Enumerated steps for this subtree      |
      | Status       | Current progress and completion % 0%   |
      | Submodules   | List of submodules and related work    |
    And the spec should be committed to git with message "Initialize spec for {nodeId}"

  @spec @validation @core
  Scenario: Spec validation ensures standard structure
    Given a spec file exists at {specPath}
    When validate(specPath) is called
    Then the spec should be checked for:
      | requirement           | example                             |
      | Required sections     | Header, Plan, Status                |
      | Well-formed Markdown  | Valid header hierarchy (#/##/###)   |
      | Submodule sections    | #/Submodules/<name> if submodules   |
      | Parseable metadata    | nodeId, timestamps in header        |
    And validation should return success or detailed error list
    And any subsequent spec tools should fail if validation fails

  @spec @summary @core
  Scenario: get_summary returns concise overview and section paths
    Given a spec file with multiple sections and submodule work
    When get_summary(specPath) is called
    Then the response should include:
      | item                | example                           |
      | Summary text        | Human-readable status overview    |
      | Section list        | Plan, Status, Submodules/auth-lib |
      | Completion %        | 45%                               |
      | Submodule statuses  | auth-lib: 60%, utils-lib: 25%     |
      | Section header paths| #/Plan, #/Status, #/Submodules   |
    And the response should fit in model context (avoid full spec load)

  @spec @get_section @core
  Scenario Outline: get_section retrieves targeted content without full spec
    Given a spec file with sections: Plan, Status, Submodules/<name>
    When get_section(specPath, "<headerPath>") is called
    Then the section content should be returned without loading entire spec
    And the response should be parseable and include subsections if any
    And for submodule sections, submodule-specific work should be included

    Examples:
      | headerPath                  |
      | #/Plan                      |
      | #/Status                    |
      | #/Submodules/auth-lib       |
      | #/Submodules/utils-lib      |

  @spec @update @streaming
  Scenario: Spec is updated as agent executes with streaming
    Given a WorkNode in RUNNING state with associated spec
    And the agent is editing code and streaming tokens
    When streaming completes
    Then the spec Status section should be updated with progress
    And the spec should be committed to git
    And the commit message should indicate status update
    And get_summary should reflect new status immediately

  @spec @submodule_sections
  Scenario: Specs track submodule-specific work
    Given a WorkNode with main repo and 2 submodules
    And submodule-specific edits are planned
    When the spec is created
    Then sections should exist:
      | section               | content                       |
      | #/Submodules/auth-lib | Work items for auth-lib       |
      | #/Submodules/utils    | Work items for utils          |
    And when agents edit submodule A, the spec should reflect it
    And when agents edit submodule B, the spec should reflect it independently

  @spec @merge @core
  Scenario: Child spec is merged into parent spec at merge time
    Given a child WorkNode with spec describing completed work
    And a parent WorkNode with spec
    When child worktree merges into parent (main + submodules)
    Then child spec should be validated
    And parent spec should be updated to include child's accomplishments
    And submodule-specific sections in child should merge into parent
    And merged spec should be committed to parent worktree
    And parent spec's Status should be updated reflecting child completion

  @spec @merge_strategy
  Scenario: Spec merging uses get_section to avoid loading full specs
    Given parent and child specs both with many sections
    When merge is decided
    Then the orchestrator should:
      | action                                            |
      | Call validate on both specs                      |
      | Call get_summary on child to understand changes  |
      | Use get_section to read only relevant child sections |
      | Merge only necessary sections into parent        |
      | Never load both full specs into context          |
    And the operation should scale to large specs

  @spec @resumability
  Scenario: Execution resumes from spec status after interruption
    Given a goal execution is in progress with multiple WorkNodes
    And some nodes are COMPLETED, some RUNNING, some PENDING
    When execution is interrupted (e.g., service restart)
    And execution resumes from the same repo root
    Then the orchestrator should:
      | action                                                   |
      | Discover existing worktrees via planning specs          |
      | Call get_summary on each spec to determine node status  |
      | Reconstruct WorkNodes in COMPLETED/PENDING/RUNNING state |
      | Resume execution at the next READY or RUNNING node      |
    And no data loss should occur
    And spec files should be the source of truth (not database)

  @spec @resumability_submodules
  Scenario: Resumability considers submodule state from specs
    Given a partially completed execution with main repo and submodule edits
    And specs in all worktrees document submodule-related work
    When resuming after interruption
    Then the system should:
      | action                                             |
      | Read main repo spec and submodule specs           |
      | Determine which submodule edits were completed   |
      | Reconstruct submodule worktree state correctly    |
      | Resume remaining submodule-related work if needed |
    And submodule pointers should be restored to correct commits

  @spec @format_options
  Scenario Outline: Spec format is standardized and consistent
    Given a spec file in format "<format>"
    When the spec is created or updated
    Then it should follow the standard structure for "<format>"
    And validate should pass on correctly formatted specs
    And tools (get_summary, get_section) should parse consistently

    Examples:
      | format   |
      | Markdown |
      | YAML     |

  @spec @large_spec_handling
  Scenario: Large specs with many submodules are managed efficiently
    Given a WorkNode with 10+ submodules and complex Plan/Status
    And the full spec would exceed model context window
    When agents work with the spec
    Then they should:
      | action                          |
      | Use get_summary for overview    |
      | Use get_section for sections    |
      | Never load entire spec at once  |
      | Query only needed information   |
    And performance should scale linearly with section count

  @spec @editing
  Scenario: Agent edits spec Plan and Status sections
    Given a WorkNode in RUNNING state
    And the agent is executing planned steps
    When each step completes
    Then the agent should:
      | action                                      |
      | Call get_section(specPath, "#/Plan")       |
      | Mark completed steps in Plan section       |
      | Call get_section(specPath, "#/Status")     |
      | Update Status with progress percentage     |
      | Commit spec changes to git                 |
    And spec updates should be atomic with code commits if possible

  @spec @validation_before_operations
  Scenario: All spec operations validate before proceeding
    Given an invalid spec file (missing required sections)
    When any operation is attempted (get_summary, get_section, merge)
    Then validate should be called first
    And if validation fails, the operation should abort
    And an error should indicate what sections are missing
    And the user should be notified to fix the spec

  @spec @audit_trail
  Scenario: Spec changes are auditable via git history
    Given a spec file that evolves through multiple WorkNode executions
    When examining git log for the spec file
    Then commits should show:
      | item              | example                           |
      | Initialization    | "Initialize spec for node-123"    |
      | Status updates    | "Update Status: 25% → 50%"        |
      | Merges            | "Merge child spec node-456"       |
      | Submodule updates | "Update Submodules/auth-lib"      |
    And the complete evolution should be reconstructible
    And diffs should show exactly what changed in each step
