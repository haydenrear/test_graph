package com.hayden.test_graph.commit_diff_context.init.indexing.ctx;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.commit_diff_context.init.indexing.IndexingK3sInitNode;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.result.error.SingleError;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
@ResettableThread
@RequiredArgsConstructor
@Profile("indexing")
public class IndexingK3sInit implements InitCtx {


    @Builder
    public record K3sClusterConfig(
            String clusterName,
            int kubeApiPort,
            String registryName,
            int registryPort,
            String networkName
    ) {}

    @Builder(toBuilder = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeploymentConfig {
        private String kafkaNamespace;
        private String minioNamespace;
        private String indexingNamespace;
        private KubeValues values;

        public String kafkaNamespace() {
            return kafkaNamespace;
        }

        public String minioNamespace() {
            return minioNamespace;
        }

        public String indexingNamespace() {
            return indexingNamespace;
        }

        public KubeValues values() {
            return values;
        }

    }

    @Builder
    public record ServiceConfig(
            boolean deployMinIO,
            boolean deployKafka,
            boolean deployOrchestrator,
            boolean deployPersister,
            Map<String, String> indexerEnvVars,
            Map<String, String> mavenRepositoryEnvVars
    ) {
        public ServiceConfig() {
            this(true, true, true, true, new HashMap<>(), new HashMap<>());
        }
    }

    private final ContextValue<K3sClusterConfig> clusterConfig;
    private final ContextValue<DeploymentConfig> deploymentConfig;
    private final ContextValue<ServiceConfig> serviceConfig;

    public record KubeValues(Path path) {}

    private Assertions assertions;
    private IndexingK3sBubble bubbleUnderlying;

    @Getter
    private final ContextValue<String> kubeConfigPath;

    public record ClusterStatus(boolean ready, Exception err) {}

    @Getter
    private final ContextValue<ClusterStatus> clusterReady;

    @Getter
    private final List<String> deployedServices = new ArrayList<>();

    public IndexingK3sInit() {
        this(
                ContextValue.empty(),
                ContextValue.empty(),
                ContextValue.empty(),
                ContextValue.empty(),
                ContextValue.empty()
        );
    }

    @Autowired
    public void setAssertions(Assertions assertions) {
        this.assertions = assertions;
    }

    @Autowired
    @ResettableThread
    public void setBubble(IndexingK3sBubble bubble) {
        this.bubbleUnderlying = bubble;
    }

    public void cdcIndexingValuesYaml(KubeValues config) {
        deploymentConfig.update(
                dep -> {
                    dep.values = config;
                    return dep;
                },
                DeploymentConfig.builder()
                        .values(config)
                        .build());
    }

    public void setClusterConfig(K3sClusterConfig config) {
        this.clusterConfig.swap(config);
    }

    public void setDeploymentConfig(DeploymentConfig config) {
        this.deploymentConfig.swap(config);
    }

    public void setServiceConfig(ServiceConfig config) {
        this.serviceConfig.swap(config);
    }

    public K3sClusterConfig getClusterConfig() {
        return this.clusterConfig.res().orElseThrow();
    }

    public DeploymentConfig getDeploymentConfig() {
        return this.deploymentConfig.res().orElseThrow();
    }

    public ServiceConfig getServiceConfig() {
        return this.serviceConfig.res().orElse(new ServiceConfig());
    }

    public void enableMinIO() {
        updateServiceConfig(cfg -> cfg.deployMinIO);
    }

    public void disableMinIO() {
        updateServiceConfig(cfg -> !cfg.deployMinIO);
    }

    public void enableKafka() {
        updateServiceConfig(cfg -> cfg.deployKafka);
    }

    public void disableKafka() {
        updateServiceConfig(cfg -> !cfg.deployKafka);
    }

    public void enableOrchestrator() {
        updateServiceConfig(cfg -> cfg.deployOrchestrator);
    }

    public void disableOrchestrator() {
        updateServiceConfig(cfg -> !cfg.deployOrchestrator);
    }

    public void enablePersister() {
        updateServiceConfig(cfg -> cfg.deployPersister);
    }

    public void disablePersister() {
        updateServiceConfig(cfg -> !cfg.deployPersister);
    }

    public void addIndexerEnvVar(String key, String value) {
        var config = getServiceConfig();
        config.indexerEnvVars().put(key, value);
        setServiceConfig(config);
    }

    public void addMavenRepositoryEnvVar(String key, String value) {
        var config = getServiceConfig();
        config.mavenRepositoryEnvVars().put(key, value);
        setServiceConfig(config);
    }

    public Map<String, String> getMavenRepositoryEnvVars() {
        return getServiceConfig().mavenRepositoryEnvVars();
    }

    public void setMavenCentralUrl(String url) {
        addMavenRepositoryEnvVar("MAVEN_URL", url);
    }

    private void updateServiceConfig(Function<ServiceConfig, Boolean> toggle) {
        var current = getServiceConfig();
        // Note: This is a simplified placeholder; actual toggle logic would be more sophisticated
        setServiceConfig(current);
    }

    public void markClusterReady() {
        this.clusterReady.swap(new ClusterStatus(true, null));
    }

    public void markClusterReadyErr() {
        markClusterReadyErr(new RuntimeException("Unknown failure."));
    }

    public void markClusterReadyErr(Exception e) {
        assertions.assertThat(false)
                .withFailMessage("Failed to deploy cluster with err %s.", SingleError.parseStackTraceToString(e))
                .isTrue();
        this.clusterReady.swap(new ClusterStatus(false, e));
    }

    public void addDeployedService(String serviceName) {
        this.deployedServices.add(serviceName);
    }

    @Override
    public IndexingK3sBubble bubble() {
        return this.bubbleUnderlying;
    }

    @Override
    public Class<IndexingK3sBubble> bubbleClazz() {
        return IndexingK3sBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof IndexingK3sInitNode;
    }

    public ContextValue<K3sClusterConfig> clusterConfig() {
        return clusterConfig;
    }

    public ContextValue<DeploymentConfig> deploymentConfig() {
        return deploymentConfig;
    }

    public ContextValue<ServiceConfig> serviceConfig() {
        return serviceConfig;
    }
}
