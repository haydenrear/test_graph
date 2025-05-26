@commit_diff_context_repo_operations @commit_diff_context_compose @all
Feature: Perform repo operations

  @add_branch @all
  Scenario Outline: add branch is called and validated.
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    When the repo is added to the database by calling commit diff context
    Then a branch with name "<branchName>" will be added to the database
    Examples:
      | repoUrl        | branchName | composePath                                                                                     |
      | work/first.tar | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |
#      | /Users/hayde/IdeaProjects/drools_test/commit-diff-context | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |


  @add_embeddings @all
  Scenario Outline: add embeddings is called and validated.
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the embeddings for the branch should be added
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    When the repo is added to the database by calling commit diff context
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Examples:
      | repoUrl                                                   | branchName | composePath                                                                                     |
      | work/first.tar                                            | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |
#      | /Users/hayde/IdeaProjects/drools_test/commit-diff-context | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |

  @add_blame_node
  @all
  Scenario Outline: add blame nodes is called and validated.
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the maximum time blame node runs is "2" minutes
    And the embeddings for the branch should be added
    And add blame nodes is called
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    When the repo is added to the database by calling commit diff context
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Then the blame node embeddings are validated to be added to the database
    Examples:
      | repoUrl                                                   | branchName | composePath                                                                                     |
#      | /Users/hayde/IdeaProjects/drools_test/commit-diff-context | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |
      | work/first.tar                                            | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |

  @add_blame_node_only
  @all
  Scenario Outline: add blame nodes is called and validated.
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And add blame nodes is called
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    When the repo is added to the database by calling commit diff context
    Then a branch with name "<branchName>" will be added to the database
    Then the blame node embeddings are validated to be added to the database
    Examples:
      | repoUrl                                                   | branchName | composePath                                                                                     |
#      | /Users/hayde/IdeaProjects/drools_test/commit-diff-context | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |
      | work/first.tar                                            | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |

  @retrieve_code_context
  @all
  Scenario Outline: call retrieve code context
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    And the maximum time blame node runs is "2" minutes
    And the embeddings for the branch should be added
    And add blame nodes is called
    And There exists an inject response type of "RERANK" in the file location "classpath:responses/rerank_response.js" for model server endpoint "/ai_suite_rerank" on port "9992"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    And There exists a response type of "INITIAL_CODE" in the file location "classpath:responses/initial_code_response.json" for model server endpoint "/ai_suite_gemini_flash_model" on port "9991"
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Then the blame node embeddings are validated to be added to the database
    Then retrieve code context data from the server with code query "Refactor with hello world"
    Examples:
      | repoUrl                                                   | branchName | composePath                                                                                     |
#      | /Users/hayde/IdeaProjects/drools_test/commit-diff-context | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |
      | work/first.tar                                            | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |

  @retrieve_code_context
  @all @check
  Scenario Outline: call retrieve code context retrieve code context
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    And the maximum time blame node runs is "2" minutes
    And the embeddings for the branch should be added
    And add blame nodes is called
    And There exists an inject response type of "RERANK" in the file location "classpath:responses/rerank_response.js" for model server endpoint "/ai_suite_rerank" on port "9992"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    And There exists a response type of "INITIAL_CODE" in the file location "classpath:responses/initial_code_response.json" for model server endpoint "/ai_suite_gemini_flash_model" on port "9991"
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Then the blame node embeddings are validated to be added to the database
    Then retrieve code context data from the server with code query as commit message "Refactor with hello world"
    Examples:
      | repoUrl                                                   | branchName | composePath                                                                                     |
#      | /Users/hayde/IdeaProjects/drools_test/commit-diff-context | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |
      | work/first.tar                                            | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |

  @retrieve_code_context
  @retrieve_code_context_embed_query
  @all @check
  Scenario Outline: call retrieve code context embed query
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    And the maximum time blame node runs is "2" minutes
    And the embeddings for the branch should be added
    And add blame nodes is called
    And There exists an inject response type of "RERANK" in the file location "classpath:responses/rerank_response.js" for model server endpoint "/ai_suite_rerank" on port "9992"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    And There exists a response type of "INITIAL_CODE" in the file location "classpath:responses/initial_code_response.json" for model server endpoint "/ai_suite_gemini_flash_model" on port "9991"
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Then the blame node embeddings are validated to be added to the database
    Then retrieve code context data from the server with code query as embedding loaded from "classpath:responses/code-query-embedding.json"
    Examples:
      | repoUrl                                                   | branchName | composePath                                                                                     |
#      | /Users/hayde/IdeaProjects/drools_test/commit-diff-context | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |
      | work/first.tar                                            | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |

  @all
  @add_branch
  @add_embeddings
  @add_blame_node
  @git_ops_async
  @check
  Scenario Outline: call retrieve code context async
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    And the maximum time blame node runs is "2" minutes
    And the git ops should be made at the same time
    And the git ops should be made asynchronously, waiting "10" seconds afterwards for them to start
    And the embeddings for the branch should be added
    And add blame nodes is called
    And There exists an inject response type of "RERANK" in the file location "classpath:responses/rerank_response.js" for model server endpoint "/ai_suite_rerank" on port "9992"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    And There exists a response type of "INITIAL_CODE" in the file location "classpath:responses/initial_code_response.json" for model server endpoint "/ai_suite_gemini_flash_model" on port "9991"
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Then the blame node embeddings are validated to be added to the database
    Then retrieve code context data from the server with code query as embedding loaded from "classpath:responses/code-query-embedding.json"
    Examples:
      | repoUrl                                                   | branchName | composePath                                                                                     |
      | work/first.tar                                            | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |
