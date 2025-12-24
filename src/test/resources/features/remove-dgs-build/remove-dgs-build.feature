@remove-dgs-build
Feature: Remove DGS from org builds
  To keep the organization build stable
  As a build maintainer
  I want all modules to build without DGS dependencies

  @us1-root-build
  Scenario: Root build completes across modules
    Given the org build configuration includes all required modules
    And build event listeners are configured
    When the root build is executed
    Then build completion events are recorded for each module
    And all expected build events are received

  @us2-codegen
  Scenario: Commit-diff model artifacts are generated once and committed
    Given the commit-diff schema is available
    And model generation listeners are configured
    When the final model generation is executed
    Then generated model artifacts are committed for commit-diff
    And all expected generation events are received

  @us3-consumers
  Scenario: Downstream modules build with updated model artifacts
    Given downstream modules reference the updated model artifacts
    And dependency resolution listeners are configured
    When downstream modules are built
    Then build completion events are recorded for those modules
    And all expected dependency events are received
