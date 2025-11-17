package com.hayden.test_graph.commit_diff_context.step_def;

import com.hayden.test_graph.commit_diff_context.init.k3s.ctx.K3sInit;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Step definitions for configuring K3s cluster deployment options.
 * Each step allows enabling/disabling specific services or setting environment variables.
 * These steps should be used before the cluster deployment step to customize the infrastructure.
 */
public class K3sDeploymentStepDef implements ResettableStep {

    @Autowired
    @ResettableThread
    private K3sInit k3sInit;

    @Given("the K3s cluster is deployed with MinIO")
    public void deployWithMinIO() {
        k3sInit.enableMinIO();
    }

    @Given("the K3s cluster is deployed without MinIO")
    public void deployWithoutMinIO() {
        k3sInit.disableMinIO();
    }

    @Given("the K3s cluster is deployed with Kafka")
    public void deployWithKafka() {
        k3sInit.enableKafka();
    }

    @Given("the K3s cluster is deployed without Kafka")
    public void deployWithoutKafka() {
        k3sInit.disableKafka();
    }

    @Given("the K3s cluster is deployed with orchestrator")
    public void deployWithOrchestrator() {
        k3sInit.enableOrchestrator();
    }

    @Given("the K3s cluster is deployed without orchestrator")
    public void deployWithoutOrchestrator() {
        k3sInit.disableOrchestrator();
    }

    @Given("the K3s cluster is deployed with persister")
    public void deployWithPersister() {
        k3sInit.enablePersister();
    }

    @Given("the K3s cluster is deployed without persister")
    public void deployWithoutPersister() {
        k3sInit.disablePersister();
    }

    @Given("the indexer environment variable {string} is set to {string}")
    public void setIndexerEnvVar(String key, String value) {
        k3sInit.addIndexerEnvVar(key, value);
    }

    @Given("the cluster name is {string}")
    public void setClusterName(String clusterName) {
        var currentConfig = k3sInit.clusterConfig().optional()
                .orElse(K3sInit.K3sClusterConfig.builder()
                        .clusterName(clusterName)
                        .kubeApiPort(6443)
                        .registryName("local-registry")
                        .registryPort(5000)
                        .networkName("test-network")
                        .build());
        
        var updatedConfig = K3sInit.K3sClusterConfig.builder()
                .clusterName(clusterName)
                .kubeApiPort(currentConfig.kubeApiPort())
                .registryName(currentConfig.registryName())
                .registryPort(currentConfig.registryPort())
                .networkName(currentConfig.networkName())
                .build();
        
        k3sInit.setClusterConfig(updatedConfig);
    }

    @Given("the kafka namespace is {string}")
    public void setKafkaNamespace(String namespace) {
        var currentConfig = k3sInit.deploymentConfig().optional()
                .orElse(K3sInit.DeploymentConfig.builder()
                        .kafkaNamespace("kafka")
                        .minioNamespace("minio")
                        .indexingNamespace("indexing")
                        .build());
        
        var updatedConfig = K3sInit.DeploymentConfig.builder()
                .kafkaNamespace(namespace)
                .minioNamespace(currentConfig.minioNamespace())
                .indexingNamespace(currentConfig.indexingNamespace())
                .build();
        
        k3sInit.setDeploymentConfig(updatedConfig);
    }

    @Given("the minio namespace is {string}")
    public void setMinioNamespace(String namespace) {
        var currentConfig = k3sInit.deploymentConfig().optional()
                .orElse(K3sInit.DeploymentConfig.builder()
                        .kafkaNamespace("kafka")
                        .minioNamespace("minio")
                        .indexingNamespace("indexing")
                        .build());
        
        var updatedConfig = K3sInit.DeploymentConfig.builder()
                .kafkaNamespace(currentConfig.kafkaNamespace())
                .minioNamespace(namespace)
                .indexingNamespace(currentConfig.indexingNamespace())
                .build();
        
        k3sInit.setDeploymentConfig(updatedConfig);
    }

    @Given("the indexing namespace is {string}")
    public void setIndexingNamespace(String namespace) {
        var currentConfig = k3sInit.deploymentConfig().optional()
                .orElse(K3sInit.DeploymentConfig.builder()
                        .kafkaNamespace("kafka")
                        .minioNamespace("minio")
                        .indexingNamespace("indexing")
                        .build());
        
        var updatedConfig = K3sInit.DeploymentConfig.builder()
                .kafkaNamespace(currentConfig.kafkaNamespace())
                .minioNamespace(currentConfig.minioNamespace())
                .indexingNamespace(namespace)
                .build();
        
        k3sInit.setDeploymentConfig(updatedConfig);
    }

    @Given("the Maven central URL is {string}")
    public void setMavenCentralUrl(String url) {
        k3sInit.setMavenCentralUrl(url);
    }

    @Given("the libs-resolver environment variable {string} is set to {string}")
    public void setMavenEnvVar(String key, String value) {
        k3sInit.addMavenRepositoryEnvVar(key, value);
    }
}
