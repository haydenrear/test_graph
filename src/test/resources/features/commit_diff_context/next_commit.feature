@commit_diff_context_next_commit
@all
Feature: Perform next commit

  @commit_diff_context_compose
  @all
  @next_commit_only_no_toolset
  @next_commit_only
  Scenario Outline: generate next commit without toolset.
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the embeddings for the branch should be added
    And the max number of commits parsed of the git repo when setting the embeddings is "300"
    And the max time parse blame tree is "5" seconds
    And the max diffs per file is "5"
    And the max files per chat item is "10"
    And the max number of chat items in the history is "20"
    And add blame nodes is called
    And a request for the next commit is provided with the commit message being provided from "classpath:responses/commit-message.json"
    And a request for the next commit is provided with the staged information being provided from "classpath:responses/staged.json"
    And a request for the next commit is provided with the contextData being provided from "classpath:responses/context-data.json"
    And a request for the next commit is provided with the previous requests being provided from "classpath:responses/previous-requests.json"
    And There exists an inject response type of "RERANK" in the file location "classpath:responses/rerank_response.js" for model server endpoint "/ai_suite_rerank" on port "9992"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    And There exists a response type of "INITIAL_CODE" in the file location "classpath:responses/initial_code_response.json" for model server endpoint "/ai_suite_gemini_flash_model" on port "9991"
    And There exists a response type of "CODEGEN" in the file location "classpath:responses/codegen_response.json" for model server endpoint "/ai_suite_gemini_flash_model" on port "9991"
    When the repo is added to the database by calling commit diff context
    And a request for the next commit is sent to the server with the next commit information provided previously
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Then the blame node embeddings are validated to be added to the database
    Then the response from retrieving next commit can be applied to the repository as a git diff
    Then the mountebank requests for the toolset existed
    Examples:
      | repoUrl                                   | branchName | composePath                                                                                     |
      | work/first.tar                            | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |
#      | /Users/hayde/IdeaProjects/test_graph_next | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |


  @commit_diff_context_compose
  @all
  @next_commit_only
  @next_commit_only_retry_initial
  Scenario Outline: generate next commit with toolset request when asking for initial code to match on.
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the max number of commits parsed of the git repo when setting the embeddings is "300"
    And the max time parse blame tree is "5" seconds
    And the max diffs per file is "5"
    And the max files per chat item is "10"
    And the max number of chat items in the history is "20"
    And the embeddings for the branch should be added
    And add blame nodes is called
    And docker container from repo "git@github.com:haydenrear/servers.git" with branch "main" is built with image name "mcp/postgres" from subdirectory "" and dockerfile "src/postgres/Dockerfile"
    And docker container from repo "git@github.com:haydenrear/servers.git" with branch "main" is built with image name "mcp/postgres" from subdirectory "" and dockerfile "src/postgres/Dockerfile"
    And docker container from repo "git@github.com:haydenrear/servers.git" with branch "main" is built with image name "mcp/sqlite" from subdirectory "src/sqlite" and dockerfile "Dockerfile"
    And a request for the next commit is provided with the commit message being provided from "classpath:responses/commit-message.json"
    And a request for the next commit is provided with the staged information being provided from "classpath:responses/staged.json"
    And a request for the next commit is provided with the contextData being provided from "classpath:responses/context-data.json"
    And a request for the next commit is provided with the previous requests being provided from "classpath:responses/previous-requests.json"
    And There exists an inject response type of "RERANK" in the file location "classpath:responses/rerank_response.js" for model server endpoint "/ai_suite_rerank" on port "9992"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    And There exists a response type of "INITIAL_CODE" in the file location "classpath:responses/toolset_response.json" for model server endpoint "/ai_suite_gemini_flash_model" on port "9991" for the "1" response
    And There exists a response type of "INITIAL_CODE" in the file location "classpath:responses/initial_code_response.json" for model server endpoint "/ai_suite_gemini_flash_model" on port "9991" for the "2" response repeated
    And There exists a response type of "CODEGEN" in the file location "classpath:responses/codegen_response.json" for model server endpoint "/ai_suite_gemini_flash_model" on port "9991" for the "1" response
    And the docker container "mcp/postgres" exists
    When the repo is added to the database by calling commit diff context
    And a request for the next commit is sent to the server with the next commit information provided previously
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Then the blame node embeddings are validated to be added to the database
    Then the response from retrieving next commit can be applied to the repository as a git diff
    Then the mountebank requests for the toolset existed
    Examples:
      | repoUrl        | branchName | composePath                                                                                     |
      | work/first.tar | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |
#      | https://github.com/kiegroup/drools.git | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |

  @commit_diff_context_compose
    @all
    @next_commit_only
    @next_commit_only_retry_codegen
  Scenario Outline: generate next commit with toolset request when asking for next commit code.
    Given docker-compose is started from "<composePath>"
    And there is a repository at the url "<repoUrl>"
    And a branch should be added "<branchName>"
    And the embeddings for the branch should be added
    And the max number of commits parsed of the git repo when setting the embeddings is "300"
    And the max time parse blame tree is "5" seconds
    And the max diffs per file is "5"
    And the max files per chat item is "10"
    And the max number of chat items in the history is "20"
    And add blame nodes is called
    And a request for the next commit is provided with the commit message being provided from "classpath:responses/commit-message.json"
    And a request for the next commit is provided with the staged information being provided from "classpath:responses/staged.json"
    And a request for the next commit is provided with the contextData being provided from "classpath:responses/context-data.json"
    And a request for the next commit is provided with the previous requests being provided from "classpath:responses/previous-requests.json"
    And There exists an inject response type of "RERANK" in the file location "classpath:responses/rerank_response.js" for model server endpoint "/ai_suite_rerank" on port "9992"
    And There exists a response type of "EMBEDDING" in the file location "classpath:responses/embedding_response.json" for model server endpoint "/ai_suite_gemini_embedding" on port "9991"
    And There exists a response type of "INITIAL_CODE" in the file location "classpath:responses/initial_code_response.json" for model server endpoint "/ai_suite_gemini_flash_model" on port "9991" for the "1" response
    And There exists a response type of "CODEGEN" in the file location "classpath:responses/toolset_response.json" for model server endpoint "/ai_suite_gemini_flash_model" on port "9991" for the "1" response
    And There exists a response type of "CODEGEN" in the file location "classpath:responses/codegen_response.json" for model server endpoint "/ai_suite_gemini_flash_model" on port "9991" for the "2" response repeated
    And the docker container "mcp/postgres" exists
    When the repo is added to the database by calling commit diff context
    And a request for the next commit is sent to the server with the next commit information provided previously
    Then a branch with name "<branchName>" will be added to the database
    Then the branches embeddings will be added to the database
    Then the blame node embeddings are validated to be added to the database
    Then the response from retrieving next commit can be applied to the repository as a git diff
    Then the mountebank requests for the toolset existed
    Examples:
      | repoUrl        | branchName | composePath                                                                                     |
      | work/first.tar | main       | /Users/hayde/IdeaProjects/drools/test_graph/src/test/docker/commit-diff-context/no-model-server |

