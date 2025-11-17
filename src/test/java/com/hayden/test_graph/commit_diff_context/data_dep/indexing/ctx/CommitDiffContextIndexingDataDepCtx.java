package com.hayden.test_graph.commit_diff_context.data_dep.indexing.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.data_dep.ctx.DataDepCtx;
import com.hayden.test_graph.commit_diff_context.data_dep.indexing.CommitDiffContextIndexingDataDepNode;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.sort.GraphSort;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@ResettableThread
@RequiredArgsConstructor
public class CommitDiffContextIndexingDataDepCtx implements DataDepCtx {

    @Builder
    public record ServiceConfig(
            String namespace,
            String image,
            Map<String, String> envVars
    ) {
        public ServiceConfig() {
            this("default", "default:latest", new HashMap<>());
        }
    }

    @Builder
    public record IndexingDeployment(
            Optional<ServiceConfig> minioConfig,
            Optional<ServiceConfig> kafkaConfig,
            Optional<ServiceConfig> orchestratorConfig,
            Optional<ServiceConfig> persisterConfig,
            Map<String, String> indexerEnvVars
    ) {
        public IndexingDeployment() {
            this(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), new HashMap<>());
        }
    }

    private final ContextValue<CommitDiffContextIndexingDataDepBubble> bubbleUnderlying;
    private final ContextValue<IndexingDeployment> deployment;

    @Getter
    private final ContextValue<String> deploymentId;

    @Getter
    private final ContextValue<Boolean> ready;

    @Getter
    private final ContextValue<String> repoUrl;

    @Getter
    private final ContextValue<String> branch;

    public CommitDiffContextIndexingDataDepCtx() {
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
    public void setBubble(CommitDiffContextIndexingDataDepBubble bubble) {
        this.bubbleUnderlying.swap(bubble);
        this.bubbleUnderlying.res().one().get().getIndexingCtx().swap(this);
    }

    public void setDeployment(IndexingDeployment deployment) {
        this.deployment.swap(deployment);
    }

    public IndexingDeployment getDeployment() {
        return this.deployment.res().orElse(new IndexingDeployment());
    }

    public void setMinioConfig(ServiceConfig config) {
        var current = getDeployment();
        var updated = IndexingDeployment.builder()
                .minioConfig(Optional.of(config))
                .kafkaConfig(current.kafkaConfig())
                .orchestratorConfig(current.orchestratorConfig())
                .persisterConfig(current.persisterConfig())
                .indexerEnvVars(current.indexerEnvVars())
                .build();
        setDeployment(updated);
    }

    public void setKafkaConfig(ServiceConfig config) {
        var current = getDeployment();
        var updated = IndexingDeployment.builder()
                .minioConfig(current.minioConfig())
                .kafkaConfig(Optional.of(config))
                .orchestratorConfig(current.orchestratorConfig())
                .persisterConfig(current.persisterConfig())
                .indexerEnvVars(current.indexerEnvVars())
                .build();
        setDeployment(updated);
    }

    public void setOrchestratorConfig(ServiceConfig config) {
        var current = getDeployment();
        var updated = IndexingDeployment.builder()
                .minioConfig(current.minioConfig())
                .kafkaConfig(current.kafkaConfig())
                .orchestratorConfig(Optional.of(config))
                .persisterConfig(current.persisterConfig())
                .indexerEnvVars(current.indexerEnvVars())
                .build();
        setDeployment(updated);
    }

    public void setPersisterConfig(ServiceConfig config) {
        var current = getDeployment();
        var updated = IndexingDeployment.builder()
                .minioConfig(current.minioConfig())
                .kafkaConfig(current.kafkaConfig())
                .orchestratorConfig(current.orchestratorConfig())
                .persisterConfig(Optional.of(config))
                .indexerEnvVars(current.indexerEnvVars())
                .build();
        setDeployment(updated);
    }

    public void addIndexerEnvVar(String key, String value) {
        var current = getDeployment();
        current.indexerEnvVars().put(key, value);
        setDeployment(current);
    }

    public void markReady() {
        this.ready.swap(true);
    }

    public void setDeploymentId(String id) {
        this.deploymentId.swap(id);
    }

    public void setRepoUrl(String url) {
        this.repoUrl.swap(url);
    }

    public void setBranch(String branchName) {
        this.branch.swap(branchName);
    }

    @Override
    public CommitDiffContextIndexingDataDepBubble bubble() {
        return this.bubbleUnderlying.res().one().get();
    }

    @Override
    public Class<CommitDiffContextIndexingDataDepBubble> bubbleClazz() {
        return CommitDiffContextIndexingDataDepBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffContextIndexingDataDepNode;
    }

    public ContextValue<IndexingDeployment> deployment() {
        return deployment;
    }
}
