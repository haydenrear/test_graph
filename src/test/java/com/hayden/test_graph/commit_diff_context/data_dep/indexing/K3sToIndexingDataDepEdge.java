package com.hayden.test_graph.commit_diff_context.data_dep.indexing;

import com.hayden.test_graph.commit_diff_context.data_dep.indexing.ctx.CommitDiffContextIndexingDataDepCtx;
import com.hayden.test_graph.commit_diff_context.init.indexing.ctx.IndexingK3sBubble;
import com.hayden.test_graph.commit_diff_context.init.indexing.ctx.IndexingK3sInit;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.sort.GraphSort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Graph edge that transfers K3s cluster configuration to the indexing data dependency phase.
 * This edge propagates cluster setup information so the indexing jobs can be deployed correctly.
 */
@Component
@ResettableThread
@Profile("indexing")
public class K3sToIndexingDataDepEdge implements CommitDiffContextIndexingDataDepNode {

    IndexingK3sInit indexingK3sInit;

    @Autowired
    @ResettableThread
    public void setK3sBubble(IndexingK3sInit indexingK3sInit) {
        this.indexingK3sInit = indexingK3sInit;
    }

    @Override
    public CommitDiffContextIndexingDataDepCtx exec(CommitDiffContextIndexingDataDepCtx c, MetaCtx h) {
        this.set(c, indexingK3sInit);
        return c;
    }

    @Override
    public List<Class<? extends CommitDiffContextIndexingDataDepNode>> dependsOn() {
        return new ArrayList<>();
    }

    public void set(CommitDiffContextIndexingDataDepCtx indexingCtx, IndexingK3sInit k3sInit) {
        // Transfer K3s service configuration to the indexing deployment context
        var serviceConfig = k3sInit.getServiceConfig();
        var deploymentConfig = k3sInit.getDeploymentConfig();

        // Transfer MinIO configuration if deployed
        if (serviceConfig.deployMinIO()) {
            indexingCtx.setMinioConfig(
                    CommitDiffContextIndexingDataDepCtx.ServiceConfig.builder()
                            .namespace(deploymentConfig.minioNamespace())
                            .image("minio:latest")
                            .build()
            );
        }

        // Transfer Kafka configuration if deployed
        if (serviceConfig.deployKafka()) {
            indexingCtx.setKafkaConfig(
                    CommitDiffContextIndexingDataDepCtx.ServiceConfig.builder()
                            .namespace(deploymentConfig.kafkaNamespace())
                            .image("kafka:latest")
                            .build()
            );
        }

        // Transfer Orchestrator configuration if deployed
        if (serviceConfig.deployOrchestrator()) {
            indexingCtx.setOrchestratorConfig(
                    CommitDiffContextIndexingDataDepCtx.ServiceConfig.builder()
                            .namespace("orchestrator")
                            .image("orchestrator:latest")
                            .build()
            );
        }

        // Transfer Persister configuration if deployed
        if (serviceConfig.deployPersister()) {
            indexingCtx.setPersisterConfig(
                    CommitDiffContextIndexingDataDepCtx.ServiceConfig.builder()
                            .namespace("persister")
                            .image("persister:latest")
                            .build()
            );
        }

        // Transfer indexer environment variables
        serviceConfig.indexerEnvVars().forEach(indexingCtx::addIndexerEnvVar);
    }
}
