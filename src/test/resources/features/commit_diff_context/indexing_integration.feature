@commit_diff_context_indexing @indexing_integration @all
Feature: End-to-end code indexing workflow

  This feature tests the complete code indexing flow:
  1. K3s cluster deployment with infrastructure
  2. Libs resolver reads Java sources from Maven and writes to MinIO
  3. Orchestrator deploys indexing job on Kubernetes
  4. Indexer (commit-diff-context-mcp) processes sources and creates indexes
  5. Persister writes indexes to database
  6. Assertions validate the complete flow

  @indexing_basic @all
  Scenario: Complete indexing workflow from Maven sources to database
    Given the K3s cluster is deployed with MinIO
    And the K3s cluster is deployed with Kafka
    And the K3s cluster is deployed with orchestrator
    And the K3s cluster is deployed with persister
    And the cluster name is "commit-diff-context-test"
    And the kafka namespace is "kafka"
    And the minio namespace is "minio"
    And the indexing namespace is "indexing"
    And the Maven central URL is "http://localhost:8080"
    When the K3s cluster is initialized
    And libs resolver reads sources from Maven artifact "com.example:my-project:1.0.0"
    And the sources are uploaded to MinIO with bucket "sources"
    And a message is published to Kafka topic "source-assignments" with source metadata
    And the orchestrator deploys an indexing job
    Then the indexing job completes successfully
    And the code indexes are published to Kafka topic "code-indexes"
    And the persister processes and stores the indexes in the database
    And the indexed file count is greater than 0
    And the symbol count is greater than 0

  @indexing_with_custom_env @all
  Scenario: Indexing with custom indexer environment variables
    Given the K3s cluster is deployed with MinIO
    And the K3s cluster is deployed with Kafka
    And the K3s cluster is deployed with orchestrator
    And the K3s cluster is deployed with persister
    And the indexer environment variable "MAX_CONTEXT_DEPTH" is set to "5"
    And the indexer environment variable "ENABLE_BLAME_TREE" is set to "true"
    And the Maven central URL is "http://localhost:8080"
    When the K3s cluster is initialized
    And libs resolver reads sources from Maven artifact "com.example:test-project:2.0.0"
    And the sources are uploaded to MinIO
    And a message is published to Kafka topic "source-assignments"
    And the orchestrator deploys an indexing job
    Then the indexing job completes successfully
    And the persister processes and stores the indexes
    And validation passes with custom environment configuration

  @indexing_without_persister @all
  Scenario: Indexing workflow without persister service
    Given the K3s cluster is deployed with MinIO
    And the K3s cluster is deployed with Kafka
    And the K3s cluster is deployed with orchestrator
    And the K3s cluster is deployed without persister
    When the K3s cluster is initialized
    And libs resolver reads sources from Maven artifact "com.example:mini-project:1.0.0"
    And the sources are uploaded to MinIO
    And a message is published to Kafka topic "source-assignments"
    And the orchestrator deploys an indexing job
    Then the indexing job completes successfully
    And the code indexes are published to Kafka topic "code-indexes"
    And the indexes can be consumed but storage is skipped

  @indexing_multiple_repos @all
  Scenario Outline: Indexing multiple repositories in sequence
    Given the K3s cluster is deployed with MinIO
    And the K3s cluster is deployed with Kafka
    And the K3s cluster is deployed with orchestrator
    And the K3s cluster is deployed with persister
    And the Maven central URL is "http://localhost:8080"
    When the K3s cluster is initialized
    And libs resolver reads sources from Maven artifact "<artifact>"
    And the sources are uploaded to MinIO with bucket "<bucket>"
    And a message is published to Kafka topic "source-assignments"
    And the orchestrator deploys an indexing job
    Then the indexing job completes successfully
    And the indexed repository "<repoName>" contains "<expectedFileCount>" files
    And the indexed repository "<repoName>" contains "<expectedSymbolCount>" symbols

    Examples:
      | artifact                        | bucket        | repoName      | expectedFileCount | expectedSymbolCount |
      | com.example:project-alpha:1.0.0 | alpha-sources | project-alpha | 150               | 2500                |
      | com.example:project-beta:2.0.0  | beta-sources  | project-beta  | 200               | 3200                |
      | com.example:project-gamma:1.5.0 | gamma-sources | project-gamma | 100               | 1800                |

  @indexing_error_handling @all
  Scenario: Error handling when indexing fails
    Given the K3s cluster is deployed with MinIO
    And the K3s cluster is deployed with Kafka
    And the K3s cluster is deployed with orchestrator
    And the K3s cluster is deployed with persister
    And the Maven central URL is "http://localhost:8080"
    When the K3s cluster is initialized
    And libs resolver reads sources from Maven artifact "com.example:broken-project:1.0.0"
    And the sources are uploaded to MinIO
    And a message is published to Kafka topic "source-assignments"
    And the orchestrator deploys an indexing job
    Then the indexing job fails gracefully
    And an error message is published to Kafka topic "indexing-errors"
    And the persister does not write incomplete indexes

  @indexing_performance @all
  Scenario: Large repository indexing performance
    Given the K3s cluster is deployed with MinIO
    And the K3s cluster is deployed with Kafka
    And the K3s cluster is deployed with orchestrator
    And the K3s cluster is deployed with persister
    And the indexer environment variable "ENABLE_PARALLEL_PROCESSING" is set to "true"
    And the Maven central URL is "http://localhost:8080"
    When the K3s cluster is initialized
    And libs resolver reads sources from Maven artifact "com.example:large-project:3.0.0"
    And the sources are uploaded to MinIO
    And a message is published to Kafka topic "source-assignments"
    And the orchestrator deploys an indexing job
    Then the indexing job completes successfully
    And the indexing time is less than "300" seconds
    And all code indexes are persisted to the database
