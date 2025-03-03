@commit_diff_context_llm_validation
#@all
Feature: Perform next commit

  @generate_next_commit_llm
  Scenario Outline:
    Given a postgres database to be loaded from "<postgresPath>" for docker-compose "<composePath>"
    And docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the embeddings for the branch should be added
    And the most recent commit is saved to memory and removed from the repository
    When the repo is added to the database by calling commit diff context
    And a request for the next commit is sent to the server with the next commit information provided previously
    And the AI generated response is compared to the actual commit by calling the validation endpoint "<validationEndpoint>"
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Then the blame node embeddings are validated to be added to the database
    Then the response from retrieving next commit can be applied to the repository as a git diff
    Then the validation data is saved for review
    Examples:
      | repoUrl                                   | postgresPath               | branchName | composePath                                                                        | validationEndpoint                      |
#      | https://github.com/kiegroup/drools.git | ~/test_dbs/drools_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |
      | /Users/hayde/IdeaProjects/test_graph_next | ~/test_dbs/drools_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |

  @do_embedding_only
  Scenario Outline:
    Given a postgres database to be loaded from "<postgresPath>" for docker-compose "<composePath>"
    And docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the embeddings for the branch should be added
    Then the branches embeddings will be added to the database
    Examples:
      | repoUrl                                   | postgresPath                   | branchName | composePath                                                                        | validationEndpoint                      |
#      | https://github.com/kiegroup/drools.git | ~/test_dbs/drools_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |
      | /Users/hayde/IdeaProjects/test_graph_next | ~/test_dbs/test_graph_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |

  @do_embedding_blame_tree_only
  Scenario Outline:
    Given a postgres database to be loaded from "<postgresPath>" for docker-compose "<composePath>"
    And docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the embeddings for the branch should be added
    And add blame nodes is called
    Then the branches embeddings will be added to the database
    Then the blame node embeddings are validated to be added to the database
    Examples:
      | repoUrl                                   | postgresPath                   | branchName | composePath                                                                        | validationEndpoint                      |
#      | https://github.com/kiegroup/drools.git | ~/test_dbs/drools_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |
      | /Users/hayde/IdeaProjects/test_graph_next | ~/test_dbs/test_graph_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |

  @test_postgres_db
  Scenario Outline:
    Given a postgres database to be loaded from "<postgresPath>" for docker-compose "<composePath>"
    And docker-compose is started from "<composePath>"
    Then postgres database should be started
    Examples:
      | postgresPath               | branchName | composePath                                                                        |
#      | ~/test_dbs/drools_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres |
      | ~/test_dbs/drools_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres |
