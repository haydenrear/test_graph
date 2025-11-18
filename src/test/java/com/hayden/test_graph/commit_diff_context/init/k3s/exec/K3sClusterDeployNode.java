package com.hayden.test_graph.commit_diff_context.init.k3s.exec;

import com.hayden.test_graph.commit_diff_context.init.k3s.K3sInitNode;
import com.hayden.test_graph.commit_diff_context.init.k3s.ctx.K3sInit;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Deploys the K3s cluster using the deployment orchestrator.
 * This node runs the helm deployment scripts via Python to set up the base infrastructure
 * that the entire integration test flow depends on, including the Kubernetes cluster,
 * Kafka, MinIO, and related services.
 */
@Component
@Slf4j
public class K3sClusterDeployNode implements K3sInitNode {

    @Override
    public K3sInit exec(K3sInit ctx, MetaCtx metaCtx) {
        log.info("Starting K3s cluster initialization");

        // Set default configuration if not already provided
        if (ctx.clusterConfig().optional().isEmpty()) {
            var defaultConfig = K3sInit.K3sClusterConfig.builder()
                    .clusterName("commit-diff-context-test")
                    .kubeApiPort(6443)
                    .registryName("local-registry")
                    .registryPort(5000)
                    .networkName("test-network")
                    .build();
            ctx.setClusterConfig(defaultConfig);
        }

        // Set default deployment namespaces
        if (ctx.deploymentConfig().optional().isEmpty()) {
            var deployConfig = K3sInit.DeploymentConfig.builder()
                    .kafkaNamespace("kafka")
                    .minioNamespace("minio")
                    .indexingNamespace("indexing")
                    .build();
            ctx.setDeploymentConfig(deployConfig);
        }

        log.info("K3s cluster configuration prepared: {}", ctx.getClusterConfig());
        log.info("Deployment namespaces configured: {}", ctx.getDeploymentConfig());

        // Mark as ready after configuration
        ctx.markClusterReady();
        log.info("K3s cluster initialization completed");
        log.info("Starting K3s cluster deployment");

        // Set default configuration if not already provided
        if (ctx.clusterConfig().optional().isEmpty()) {
            var defaultConfig = K3sInit.K3sClusterConfig.builder()
                    .clusterName("commit-diff-context-test")
                    .kubeApiPort(6443)
                    .registryName("local-registry")
                    .registryPort(5000)
                    .networkName("test-network")
                    .build();
            ctx.setClusterConfig(defaultConfig);
        }

        // Set default deployment namespaces
        if (ctx.deploymentConfig().optional().isEmpty()) {
            var deployConfig = K3sInit.DeploymentConfig.builder()
                    .kafkaNamespace("kafka")
                    .minioNamespace("minio")
                    .indexingNamespace("indexing")
                    .build();
            ctx.setDeploymentConfig(deployConfig);
        }

        log.info("K3s cluster configuration: {}", ctx.getClusterConfig());
        log.info("Deployment namespaces: {}", ctx.getDeploymentConfig());

        try {
            // Run the deployment orchestrator via Python uv
            // The actual orchestration would call the deploy_helm/__main__.py with appropriate args
            deployK3sCluster(ctx);
            ctx.markClusterReady();
            log.info("K3s cluster deployment completed successfully");
        } catch (Exception e) {
            log.error("K3s cluster deployment failed", e);
            throw new RuntimeException("Failed to deploy K3s cluster", e);
        }

        return ctx;
    }

    private void deployK3sCluster(K3sInit ctx) {
        // This would invoke the deployment orchestrator
        // For now, this is a placeholder that would be filled in with actual deployment logic
        log.debug("Deploying K3s cluster with config: {}", ctx.getClusterConfig());

        // TODO: Integrate with deploy-helm module to run orchestration
        // Example: ProcessBuilder pb = new ProcessBuilder("uv", "run", "deploy-helm/__main__.py", ...);
        throw new RuntimeException("Forgot to build the cluster deploy script!");
    }
}
