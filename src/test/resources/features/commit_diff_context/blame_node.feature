@commit_diff_context
Feature: Add blame node context for repo
  Scenario Outline:
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>" with branch "<branchName>" checked out and next commit message from user "<commitMessage>"
    When the user requests to get the next commit
    Then the model responds with valid commit that is committed to the repository successfully
  Examples:
    | repoUrl                                                                    | branchName | composePath                                          |
    | /Users/hayde/IdeaProjects/drools/commit-diff-context/test_repos/first/.git | main       | /Users/hayde/IdeaProjects/drools/model-server/docker |