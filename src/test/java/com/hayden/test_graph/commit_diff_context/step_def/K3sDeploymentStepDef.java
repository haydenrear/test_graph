package com.hayden.test_graph.commit_diff_context.step_def;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.init.indexing.ctx.IndexingK3sInit;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Step definitions for configuring K3s cluster deployment options.
 * Each step allows enabling/disabling specific services or setting environment variables.
 * These steps should be used before the cluster deployment step to customize the infrastructure.
 */
public class K3sDeploymentStepDef implements ResettableStep {

    @Autowired
    @ResettableThread
    private IndexingK3sInit indexingK3SInit;

    @Autowired
    @ResettableThread
    private Assertions assertions;

    @Given("the K3s cluster is deployed with MinIO")
    public void deployWithMinIO() {
        indexingK3SInit.enableMinIO();
    }

    @And("the values.yaml file for the indexing test is located in {string}")
    public void persisterProcessesIndexes(String valuesYaml) {
        Path path = Paths.get(valuesYaml);
        assertions.assertThat(path.toFile())
                .withFailMessage("Path for values yaml %s was supposed to exist!", valuesYaml)
                .exists();
        this.indexingK3SInit.cdcIndexingValuesYaml(new IndexingK3sInit.KubeValues(path));
    }


    @Given("the K3s cluster is deployed without MinIO")
    public void deployWithoutMinIO() {
//        still needed for assertions...
        indexingK3SInit.disableMinIO();
    }

    @Given("the K3s cluster is deployed with Kafka")
    public void deployWithKafka() {
//        still needed for assertions...
        indexingK3SInit.enableKafka();
    }

    @Given("the K3s cluster is deployed without Kafka")
    public void deployWithoutKafka() {
//        still needed for assertions...
        indexingK3SInit.disableKafka();
    }

    @Given("the K3s cluster is deployed with orchestrator")
    public void deployWithOrchestrator() {
        indexingK3SInit.enableOrchestrator();
    }

    @Given("the K3s cluster is deployed without orchestrator")
    public void deployWithoutOrchestrator() {
        indexingK3SInit.disableOrchestrator();
    }

    @Given("the K3s cluster is deployed with persister")
    public void deployWithPersister() {
        indexingK3SInit.enablePersister();
    }

    @Given("the K3s cluster is deployed without persister")
    public void deployWithoutPersister() {
        indexingK3SInit.disablePersister();
    }

    @Given("the indexer environment variable {string} is set to {string}")
    public void setIndexerEnvVar(String key, String value) {
        indexingK3SInit.addIndexerEnvVar(key, value);
    }

    @Given("the cluster name is {string}")
    public void setClusterName(String clusterName) {
        var currentConfig = indexingK3SInit.clusterConfig().optional()
                .orElse(IndexingK3sInit.K3sClusterConfig.builder()
                        .clusterName(clusterName)
                        .kubeApiPort(6443)
                        .registryName("local-registry")
                        .registryPort(5000)
                        .networkName("test-network")
                        .build());
        
        var updatedConfig = IndexingK3sInit.K3sClusterConfig.builder()
                .clusterName(clusterName)
                .kubeApiPort(currentConfig.kubeApiPort())
                .registryName(currentConfig.registryName())
                .registryPort(currentConfig.registryPort())
                .networkName(currentConfig.networkName())
                .build();
        
        indexingK3SInit.setClusterConfig(updatedConfig);
    }

    @Given("the kafka namespace is {string}")
    public void setKafkaNamespace(String namespace) {
        var currentConfig = indexingK3SInit.deploymentConfig().optional()
                .orElse(IndexingK3sInit.DeploymentConfig.builder()
                        .kafkaNamespace("kafka")
                        .minioNamespace("minio")
                        .indexingNamespace("indexing")
                        .build());
        
        var updatedConfig = IndexingK3sInit.DeploymentConfig.builder()
                .kafkaNamespace(namespace)
                .minioNamespace(currentConfig.minioNamespace())
                .indexingNamespace(currentConfig.indexingNamespace())
                .build();
        
        indexingK3SInit.setDeploymentConfig(updatedConfig);
    }

    @Given("the minio namespace is {string}")
    public void setMinioNamespace(String namespace) {
        var currentConfig = indexingK3SInit.deploymentConfig().optional()
                .orElse(IndexingK3sInit.DeploymentConfig.builder()
                        .kafkaNamespace("kafka")
                        .minioNamespace("minio")
                        .indexingNamespace("indexing")
                        .build());
        
        var updatedConfig = IndexingK3sInit.DeploymentConfig.builder()
                .kafkaNamespace(currentConfig.kafkaNamespace())
                .minioNamespace(namespace)
                .indexingNamespace(currentConfig.indexingNamespace())
                .build();
        
        indexingK3SInit.setDeploymentConfig(updatedConfig);
    }

    @Given("the indexing namespace is {string}")
    public void setIndexingNamespace(String namespace) {
        var currentConfig = indexingK3SInit.deploymentConfig().optional()
                .orElse(IndexingK3sInit.DeploymentConfig.builder()
                        .kafkaNamespace("kafka")
                        .minioNamespace("minio")
                        .indexingNamespace("indexing")
                        .build());
        
        var updatedConfig = IndexingK3sInit.DeploymentConfig.builder()
                .kafkaNamespace(currentConfig.kafkaNamespace())
                .minioNamespace(currentConfig.minioNamespace())
                .indexingNamespace(namespace)
                .build();
        
        indexingK3SInit.setDeploymentConfig(updatedConfig);
    }

    @Given("the Maven central URL is {string}")
    public void setMavenCentralUrl(String url) {
        indexingK3SInit.setMavenCentralUrl(url);
    }

    @Given("the libs-resolver environment variable {string} is set to {string}")
    public void setMavenEnvVar(String key, String value) {
        indexingK3SInit.addMavenRepositoryEnvVar(key, value);
    }
}
