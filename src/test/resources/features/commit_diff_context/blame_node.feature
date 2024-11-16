@all @commit_diff_context
Feature: Add blame node context for repo
  @commit_diff_context_compose
  Scenario Outline:
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    When the repo is added to the database by calling commit diff context
    When the user requests to get the next commit with commit message "<commitMessage>"
    Then the model responds with valid commit that is committed to the repository successfully
    Examples:
      | repoUrl                                                                    | branchName | composePath                                                                                  | commitMessage |
      | /Users/hayde/IdeaProjects/drools/commit-diff-context/test_repos/first/.git | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/model-server | hello!        |

