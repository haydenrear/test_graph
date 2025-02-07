@llm_validation
Feature: Perform next commit

  Scenario Outline:
    Given a postgres database to be loaded from "<postgresPath>" for docker-compose "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the embeddings for the branch should be added
    And the most recent commit is saved to memory and removed from the repository
    When the repo is added to the database by calling commit diff context
    And a request for the next commit is sent to the server with the next commit information provided previously
    And the AI generated response is compared to the actual commit by calling the validation endpoint "<validationEndpoint>"
    Then the validation score is saved to file for review
    Examples:
      | repoUrl                                | postgresPath               | branchName | composePath                                                                        | validationEndpoint                      |
      | https://github.com/kiegroup/drools.git | ~/test_dbs/drools_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |


