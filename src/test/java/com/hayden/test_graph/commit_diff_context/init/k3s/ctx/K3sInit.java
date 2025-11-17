package com.hayden.test_graph.commit_diff_context.init.k3s.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.commit_diff_context.init.k3s.K3sInitNode;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ResettableThread
@RequiredArgsConstructor
public class K3sInit implements InitCtx {

    @Builder
    public record K3sClusterConfig(
            String clusterName,
            int kubeApiPort,
            String registryName,
            int registryPort,
            String networkName
    ) {}

    @Builder
    public record DeploymentConfig(
            String kafkaNamespace,
            String minioNamespace,
            String indexingNamespace
    ) {}

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
    private final ContextValue<K3sBubble> bubbleUnderlying;

    @Getter
    private final ContextValue<String> kubeConfigPath;

    @Getter
    private final ContextValue<Boolean> clusterReady;

    @Getter
    private final List<String> deployedServices = new ArrayList<>();

    public K3sInit() {
        this(
                ContextValue.empty(),
                ContextValue.empty(),
                ContextValue.empty(),
                ContextValue.empty(),
                ContextValue.empty(),
                ContextValue.empty()
        );
    }

    @Autowired
    public void setBubble(K3sBubble bubble) {
        this.bubbleUnderlying.swap(bubble);
        this.bubbleUnderlying.res().one().get().getK3sInit().swap(this);
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

    private void updateServiceConfig(java.util.function.Function<ServiceConfig, Boolean> toggle) {
        var current = getServiceConfig();
        // Note: This is a simplified placeholder; actual toggle logic would be more sophisticated
        setServiceConfig(current);
    }

    public void markClusterReady() {
        this.clusterReady.swap(true);
    }

    public void addDeployedService(String serviceName) {
        this.deployedServices.add(serviceName);
    }

    @Override
    public K3sBubble bubble() {
        return this.bubbleUnderlying.res().one().get();
    }

    @Override
    public Class<K3sBubble> bubbleClazz() {
        return K3sBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof K3sInitNode;
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
