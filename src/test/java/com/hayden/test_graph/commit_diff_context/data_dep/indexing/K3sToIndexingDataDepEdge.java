package com.hayden.test_graph.commit_diff_context.data_dep.indexing;

import com.hayden.test_graph.commit_diff_context.data_dep.indexing.ctx.CommitDiffContextIndexingDataDepCtx;
import com.hayden.test_graph.commit_diff_context.init.k3s.ctx.K3sBubble;
import com.hayden.test_graph.graph.edge.PostExecHyperGraphEdge;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Graph edge that transfers K3s cluster configuration to the indexing data dependency phase.
 * This edge propagates cluster setup information so the indexing jobs can be deployed correctly.
 */
@Component
@ResettableThread
public class K3sToIndexingDataDepEdge implements CommitDiffContextIndexingDataDepNode {

    K3sBubble k3sBubble;

    @Autowired
    @ResettableThread
    public void setK3sBubble(K3sBubble k3sBubble) {
        this.k3sBubble = k3sBubble;
    }

    @Override
    public CommitDiffContextIndexingDataDepCtx exec(CommitDiffContextIndexingDataDepCtx c, MetaCtx h) {
        this.set(c, k3sBubble);
        return c;
    }

    public void set(CommitDiffContextIndexingDataDepCtx indexingCtx, K3sBubble k3sBubble) {
        // Transfer K3s service configuration to the indexing deployment context
        k3sBubble.getK3sInit().optional()
                .ifPresent(k3sInit -> {
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
                });
    }
}
