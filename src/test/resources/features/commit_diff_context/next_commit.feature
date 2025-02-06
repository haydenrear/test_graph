@commit_diff_context_repo_operations
#@all
Feature: Perform next commit

  @commit_diff_context_compose
#  @all
  @next_commit_only
  Scenario Outline:
#    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the embeddings for the branch should be added
    And a request for the next commit is provided for the given url and branch name provided
    And a request for the next commit is provided with the commit message being provided from "classpath:requests/commit-message.json"
    And a request for the next commit is provided with the staged information being provided from "classpath:requests/staged.json"
    And a request for the next commit is provided with the contextData being provided from "classpath:requests/context-data.json"
    And a request for the next commit is provided with the previous requests being provided from "classpath:requests/previous-requests.json"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    And There exists a response type of "CODEGEN" in the file location "classpath:responses/codegen_response.json" for model server endpoint "/ai_suite_gemini_flash_model" on port "9991"
    When the repo is added to the database by calling commit diff context
    And a request for the next commit is sent to the server with the next commit information provided previously
    Then a branch with name "<branchName>" will be added to the database
    Then the response from retrieving next commit can be applied to the repository as a git diff
    Examples:
      | repoUrl                                                               | branchName | composePath                                                                                     | addUrlQueryPath                                                               |
      | /Users/hayde/IdeaProjects/drools/commit-diff-context/test_repos/first | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server | /Users/hayde/IdeaProjects/drools/test_graph/src/test/resource/addRepo.graphql |