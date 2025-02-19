@commit_diff_context_repo_operations @commit_diff_context_compose @all
Feature: Perform repo operations

  @add_branch @all
  Scenario Outline: add branch is called and validated.
#    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    When the repo is added to the database by calling commit diff context
    Then a branch with name "<branchName>" will be added to the database
    Examples:
      | repoUrl        | branchName | composePath                                                                                     |
      | work/first.tar | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |

  @add_embeddings @all
  Scenario Outline: add embeddings is called and validated.
#    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the embeddings for the branch should be added
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    When the repo is added to the database by calling commit diff context
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Examples:
      | repoUrl        | branchName | composePath                                                                                     |
      | work/first.tar | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |

  @add_blame_node
  @all
  Scenario Outline: add blame nodes is called and validated.
#    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the embeddings for the branch should be added
    And add blame nodes is called
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    When the repo is added to the database by calling commit diff context
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Then the blame node embeddings are validated to be added to the database
    Examples:
      | repoUrl        | branchName | composePath                                                                                     |
      | work/first.tar | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |
