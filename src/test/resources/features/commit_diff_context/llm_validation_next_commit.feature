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
    And add blame nodes is called
    And a request for the next commit is provided with the contextData being provided from "classpath:responses/context-data.json"
    And the max number of commits parsed of the git repo when setting the embeddings is "300"
    And the most recent commit is saved to memory and removed from the repository
    When the repo is added to the database by calling commit diff context
    And a request for the next commit is sent to the server with the next commit information provided previously
#    And the AI generated response is compared to the actual commit by calling the validation endpoint "<validationEndpoint>"
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Then the blame node embeddings are validated to be added to the database
    Then the response from retrieving next commit can be applied to the repository as a git diff
    Then the validation data is saved for review
    Examples:
      | repoUrl                                | postgresPath               | branchName | composePath                                                                        | validationEndpoint                      |
      | https://github.com/kiegroup/drools.git | ~/test_dbs/drools_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |
#      | /Users/hayde/IdeaProjects/test_graph_next | ~/test_dbs/drools_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |

  @generate_apply_next_commit
  Scenario Outline:
    Given a postgres database to be loaded from "<postgresPath>" for docker-compose "<composePath>"
    And docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And a request for the next commit is provided with the contextData being provided from "classpath:responses/context-data.json"
    And a request for the next commit is provided with the commit message being provided from "classpath:responses/commit-message.json"
    And the staged commit information is retrieved from the repository
    And the max number of commits parsed of the git repo when setting the embeddings is "300"
    And the max time parse blame tree is "5" seconds
    And the max diffs per file is "5"
    And the max files per chat item is "10"
    And the max number of chat items in the history is "20"
    And blame tree is not parsed for next commit
    And the repository "/Users/hayde/IdeaProjects/drools_test/commit-diff-model" with branch "main" can be used in the context
    And a request for the next commit is sent to the server with the next commit information provided previously
#    And the AI generated response is compared to the actual commit by calling the validation endpoint "<validationEndpoint>"
    Then the response from retrieving next commit can be applied to the repository as a git diff
    Then the validation data is saved for review
    Examples:
      | repoUrl                                | postgresPath               | branchName | composePath                                                                        | validationEndpoint                      |
#      | https://github.com/kiegroup/drools.git | ~/test_dbs/drools_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |
#      | /Users/hayde/IdeaProjects/test_graph_next | ~/test_dbs/drools_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |
      | /Users/hayde/IdeaProjects/drools_test/commit-diff-context | ~/test_dbs/test_graph_postgres | main       | ~/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/load-postgres | /ai_suite_gemini_flash_model_validation |

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
