@all @commit_diff_context_repo_operations
Feature: Perform repo operations

  @commit_diff_context_compose @all @repo_op
  Scenario Outline:
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/gemini_embedding" on port "9991"
    When the repo is added to the database by calling commit diff context
    Then a branch with name "<branchName>" will be added to the database
    Examples:
      | repoUrl                                                                    | branchName | composePath                                                                                     | addUrlQueryPath                                                               |
      | /Users/hayde/IdeaProjects/drools/commit-diff-context/test_repos/first/.git | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server | /Users/hayde/IdeaProjects/drools/test_graph/src/test/resource/addRepo.graphql |


  Scenario Outline:
#    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the embeddings for the branch should be added
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    When the repo is added to the database by calling commit diff context
    Then a branch with name "<branchName>" will be added to the database
    Then the branch will be added to the database
    Then the branches embeddings will be added to the database
    Examples:
      | repoUrl                                                               | branchName | composePath                                                                                     | addUrlQueryPath                                                               |
      | /Users/hayde/IdeaProjects/drools/commit-diff-context/test_repos/first | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server | /Users/hayde/IdeaProjects/drools/test_graph/src/test/resource/addRepo.graphql |
