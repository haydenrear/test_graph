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

  @add_episodic_memory
  @all
  Scenario Outline: add episodic memory is called and validated.
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the maximum time episodic memory runs is "2" minutes
    And the embeddings for the branch should be added
    And add episodic memory is called
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    When the repo is added to the database by calling commit diff context
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Then the episodic memory embeddings are validated to be added to the database
    Examples:
      | repoUrl                                                   | branchName | composePath                                                                                     |
#      | /Users/hayde/IdeaProjects/drools_test/commit-diff-context | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |
      | work/first.tar                                            | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |

  @add_episodic_memory_only
  @all
  Scenario Outline: add episodic memory is called and validated.
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And add episodic memory is called
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    When the repo is added to the database by calling commit diff context
    Then a branch with name "<branchName>" will be added to the database
    Then the episodic memory embeddings are validated to be added to the database
    Examples:
      | repoUrl                                                   | branchName | composePath                                                                                     |
#      | /Users/hayde/IdeaProjects/drools_test/commit-diff-context | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |
      | work/first.tar                                            | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |

  @all
  @add_branch
  @add_embeddings
  @add_episodic_memory
  @git_ops_async
  @check
  Scenario Outline: call retrieve code context async
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    And the maximum time episodic memory runs is "2" minutes
    And the git ops should be made at the same time
    And the git ops should be made asynchronously, waiting "10" seconds afterwards for them to start
    And the embeddings for the branch should be added
    And add episodic memory is called
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Then the episodic memory embeddings are validated to be added to the database
    Examples:
      | repoUrl                                                   | branchName | composePath                                                                                     |
      | work/first.tar                                            | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |
