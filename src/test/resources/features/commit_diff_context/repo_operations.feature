@all @commit_diff_context_repo_operations
Feature: Add blame node context for repo

  @commit_diff_context_compose
  Scenario Outline:
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    When the repo is added to the database by calling commit diff context
    Then a branch with name "<branchName>" will be added to the database
    Examples:
      | repoUrl                                                                    | branchName | composePath                                                                                     | addUrlQueryPath                                                               |
      | /Users/hayde/IdeaProjects/drools/commit-diff-context/test_repos/first/.git | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server | /Users/hayde/IdeaProjects/drools/test_graph/src/test/resource/addRepo.graphql |
