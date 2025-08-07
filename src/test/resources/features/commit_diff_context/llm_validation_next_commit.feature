@commit_diff_context_llm_validation
#@all
Feature: Perform next commit

  @do_embedding_only
  Scenario Outline:
    Given a postgres database to be loaded from "<postgresPath>" for docker-compose "<composePath>"
    And docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the embeddings for the branch should be added
    Then the branches embeddings will be added to the database
    Examples:
      | repoUrl                                                   | postgresPath                   | branchName | composePath                                                                        | validationEndpoint                      |
#      | https://github.com/kiegroup/drools.git                    | ~/test_dbs/drools_postgres     | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |
#      | /Users/hayde/IdeaProjects/drools_test/test_graph          | ~/test_dbs/test_graph_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |
      | /Users/hayde/IdeaProjects/drools_test/commit-diff-model   | ~/test_dbs/test_graph_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |
      | /Users/hayde/IdeaProjects/drools_test/commit-diff-context | ~/test_dbs/test_graph_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |
      | /Users/hayde/IdeaProjects/drools_test/commit-diff-client  | ~/test_dbs/test_graph_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |

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
      | repoUrl                                                   | postgresPath                   | branchName | composePath                                                                        | validationEndpoint                      |
#      | https://github.com/kiegroup/drools.git                    | ~/test_dbs/drools_postgres     | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |
#      | /Users/hayde/IdeaProjects/drools_test/test_graph          | ~/test_dbs/test_graph_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |
#      | /Users/hayde/IdeaProjects/drools_test/commit-diff-model   | ~/test_dbs/test_graph_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |
#      | /Users/hayde/IdeaProjects/drools_test/commit-diff-context | ~/test_dbs/test_graph_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |
      | /Users/hayde/IdeaProjects/drools_test/commit-diff-client  | ~/test_dbs/test_graph_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |

  @test_postgres_db
  Scenario Outline:
    Given a postgres database to be loaded from "<postgresPath>" for docker-compose "<composePath>"
    And docker-compose is started from "<composePath>"
    Then postgres database should be started
    Examples:
      | postgresPath               | branchName | composePath                                                                        |
#      | ~/test_dbs/drools_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres |
      | ~/test_dbs/drools_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres |
