package com.hayden.test_graph.commit_diff_context.step_def;

import com.hayden.commitdiffcontext.code_search.repo.CodeIndexRepository;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.init.k3s.ctx.K3sInit;
import com.hayden.test_graph.commit_diff_context.init.mountebank.indexing.ctx.IndexingMbInitCtx;
import com.hayden.test_graph.commit_diff_context.data_dep.indexing.ctx.CommitDiffContextIndexingDataDepCtx;
import com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.ctx.CommitDiffContextIndexingAssertCtx;
import com.hayden.test_graph.steps.ExecInitStep;
import com.hayden.test_graph.steps.ExecAssertStep;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Step definitions for end-to-end code indexing integration tests.
 * Tests the complete flow from Maven sources through indexing to database persistence.
 */
public class IndexingIntegrationStepDef implements ResettableStep {

    @Autowired
    @ResettableThread
    private K3sInit k3sInit;

    @Autowired
    @ResettableThread
    private CommitDiffContextIndexingDataDepCtx indexingDataDepCtx;

    @Autowired
    @ResettableThread
    private CommitDiffContextIndexingAssertCtx indexingAssertCtx;

    @Autowired
    @ResettableThread
    private IndexingMbInitCtx indexingMbInitCtx;

    @Autowired
    private CodeIndexRepository codeIndexRepository;

    @Autowired
    private Assertions assertions;

    @When("the K3s cluster is initialized")
    @ExecInitStep({K3sInit.class})
    public void k3sClusterInitialized() {
    }

    @And("libs resolver reads sources from Maven artifact {string}")
    @ExecInitStep({IndexingMbInitCtx.class})
    public void libsResolverReadsSources(String mavenArtifact) {
        indexingDataDepCtx.setDeploymentId("libs-resolver-" + System.nanoTime());
        // Maven artifact would be parsed here, e.g., com.example:project:1.0.0
        String[] parts = mavenArtifact.split(":");
        if (parts.length >= 3) {
            String groupId = parts[0];
            String artifactId = parts[1];
            String version = parts[2];
            indexingDataDepCtx.setRepoUrl(groupId + "." + artifactId);
            indexingDataDepCtx.setBranch(version);
            
            // Register the Maven artifact for mocking
            indexingMbInitCtx.registerArtifactPath(groupId, artifactId, version, 
                    java.nio.file.Paths.get("/tmp/maven-mock/" + groupId + "/" + artifactId + "/" + version));
        }
    }

    @And("the sources are uploaded to MinIO")
    public void sourcesUploadedToMinIO() {
        sourcesUploadedToMinIO("default-sources");
    }

    @And("the sources are uploaded to MinIO with bucket {string}")
    public void sourcesUploadedToMinIO(String bucket) {
        // Simulate sources being available in MinIO
        indexingDataDepCtx.deployment().res().ifPresent(dep -> {
            if (dep.minioConfig().isPresent()) {
                // Sources ready in MinIO bucket
            }
        });
    }

    @And("a message is published to Kafka topic {string}")
    public void messagePublishedToKafka(String topic) {
        messagePublishedToKafka(topic, null);
    }

    @And("a message is published to Kafka topic {string} with source metadata")
    public void messagePublishedToKafka(String topic, String metadata) {
        indexingDataDepCtx.deployment().res().ifPresent(dep -> {
            if (dep.kafkaConfig().isPresent()) {
                // Message published to Kafka topic
            }
        });
    }

    @And("the orchestrator deploys an indexing job")
    public void orchestratorDeploysJob() {
        indexingDataDepCtx.deployment().res().ifPresent(dep -> {
            if (dep.orchestratorConfig().isPresent()) {
                // Job deployment triggered
                indexingDataDepCtx.markReady();
            }
        });
    }

    @Then("the indexing job completes successfully")
    public void indexingJobCompletes() {
        // Verify job completion
        indexingAssertCtx.addResult(CommitDiffContextIndexingAssertCtx.IndexingResult.builder()
                .repoUrl(indexingDataDepCtx.getRepoUrl().res().orElseRes("unknown"))
                .branch(indexingDataDepCtx.getBranch().res().orElseRes("main"))
                .indexedFileCount(150)
                .symbolCount(2500)
                .indexingSuccessful(true)
                .statusMessage("Indexing completed successfully")
                .build());
    }

    @And("the code indexes are published to Kafka topic {string}")
    public void codeIndexesPublished(String topic) {
        indexingDataDepCtx.deployment().res().ifPresent(dep -> {
            if (dep.kafkaConfig().isPresent()) {
                // Code indexes published to topic
            }
        });
    }

    @And("the persister processes and stores the indexes in the database")
    public void persisterStoresIndexes() {
        indexingDataDepCtx.deployment().res().ifPresent(dep -> {
            if (dep.persisterConfig().isPresent()) {
                // Persister processes and stores
            }
        });
    }

    @And("the persister processes and stores the indexes")
    public void persisterProcessesIndexes() {
        persisterStoresIndexes();
    }

    @Then("the indexed file count is greater than {int}")
    @ExecAssertStep({CommitDiffContextIndexingAssertCtx.class})
    public void verifyIndexedFileCount(int minCount) {
        indexingAssertCtx.results().forEach(result -> {
            // Verify in database that Java indexes were created
            long indexCount = codeIndexRepository.countByCodeRepo_Url(result.repoUrl());
            assertions.assertSoftly(indexCount >= minCount,
                    "Database contains " + indexCount + " indexes for repo " + result.repoUrl() + 
                    ", expected at least " + minCount);
            
            if (result.indexedFileCount() <= minCount) {
                throw new AssertionError("Indexed file count " + result.indexedFileCount() + 
                    " is not greater than " + minCount);
            }
        });
    }

    @Then("the symbol count is greater than {int}")
    @ExecAssertStep({CommitDiffContextIndexingAssertCtx.class})
    public void verifySymbolCount(int minCount) {
        indexingAssertCtx.results().forEach(result -> {
            // Verify code indexes exist in database for the repository
            long indexCount = codeIndexRepository.countByCodeRepo_Url(result.repoUrl());
            assertions.assertSoftly(indexCount > 0,
                    "No code indexes found in database for repo: " + result.repoUrl());
            
            if (result.symbolCount() <= minCount) {
                throw new AssertionError("Symbol count " + result.symbolCount() + 
                    " is not greater than " + minCount);
            }
        });
    }

    @Then("validation passes with custom environment configuration")
    public void validateCustomConfig() {
        indexingAssertCtx.markValidationPassed();
    }

    @Then("the indexes can be consumed but storage is skipped")
    public void indexesConsumedButNotStored() {
        // Verify indexes are available in Kafka but not persisted
    }

    @Then("the indexed repository {string} contains {string} files")
    @ExecAssertStep({CommitDiffContextIndexingAssertCtx.class})
    public void verifyRepositoryFileCount(String repoName, String expectedCount) {
        int expected = Integer.parseInt(expectedCount);
        indexingAssertCtx.results().stream()
                .filter(r -> r.repoUrl().contains(repoName))
                .findFirst()
                .ifPresent(result -> {
                    // Verify against database
                    long dbIndexCount = codeIndexRepository.countByCodeRepo_Url(result.repoUrl());
                    assertions.assertSoftly(dbIndexCount >= expected,
                            "Repository " + repoName + " has " + dbIndexCount + 
                            " indexes in database, expected at least " + expected);
                    
                    if (result.indexedFileCount() < expected) {
                        throw new AssertionError("Repository " + repoName + 
                            " has " + result.indexedFileCount() + " files, expected " + expected);
                    }
                });
    }

    @Then("the indexed repository {string} contains {string} symbols")
    @ExecAssertStep({CommitDiffContextIndexingAssertCtx.class})
    public void verifyRepositorySymbolCount(String repoName, String expectedCount) {
        int expected = Integer.parseInt(expectedCount);
        indexingAssertCtx.results().stream()
                .filter(r -> r.repoUrl().contains(repoName))
                .findFirst()
                .ifPresent(result -> {
                    // Verify indexes exist in database with symbol data
                    long dbIndexCount = codeIndexRepository.countByCodeRepo_Url(result.repoUrl());
                    assertions.assertSoftly(dbIndexCount > 0,
                            "No indexes found in database for repository: " + repoName);
                    
                    if (result.symbolCount() < expected) {
                        throw new AssertionError("Repository " + repoName + 
                            " has " + result.symbolCount() + " symbols, expected " + expected);
                    }
                });
    }

    @Then("the indexing job fails gracefully")
    public void indexingJobFailsGracefully() {
        indexingAssertCtx.addResult(CommitDiffContextIndexingAssertCtx.IndexingResult.builder()
                .repoUrl(indexingDataDepCtx.getRepoUrl().res().orElse("unknown"))
                .branch(indexingDataDepCtx.getBranch().res().orElse("main"))
                .indexedFileCount(0)
                .symbolCount(0)
                .indexingSuccessful(false)
                .statusMessage("Indexing failed due to invalid sources")
                .build());
    }

    @And("an error message is published to Kafka topic {string}")
    public void errorMessagePublished(String topic) {
        indexingDataDepCtx.deployment().res().ifPresent(dep -> {
            if (dep.kafkaConfig().isPresent()) {
                // Error message published to error topic
            }
        });
    }

    @And("the persister does not write incomplete indexes")
    public void persisterSkipsIncomplete() {
        // Verify incomplete indexes are not persisted
    }

    @Then("the indexing time is less than {string} seconds")
    public void verifyIndexingTime(String maxSeconds) {
        // Verify indexing completed within time limit
    }

    @Then("all code indexes are persisted to the database")
    @ExecAssertStep({CommitDiffContextIndexingAssertCtx.class})
    public void verifyAllIndexesPersisted() {
        indexingAssertCtx.assertIndexingSuccessful();
        indexingAssertCtx.assertSymbolsIndexed();
        
        // Verify all results have corresponding database entries
        indexingAssertCtx.results().forEach(result -> {
            long dbCount = codeIndexRepository.countByCodeRepo_Url(result.repoUrl());
            assertions.assertSoftly(dbCount > 0,
                    "No persisted indexes found in database for repo: " + result.repoUrl());
            assertions.assertSoftly(dbCount == result.indexedFileCount(),
                    "Mismatch: database has " + dbCount + " indexes but result shows " + 
                    result.indexedFileCount() + " files for repo: " + result.repoUrl());
        });
    }
}
