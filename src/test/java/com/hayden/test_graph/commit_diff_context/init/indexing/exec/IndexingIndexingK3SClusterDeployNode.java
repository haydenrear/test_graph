package com.hayden.test_graph.commit_diff_context.init.indexing.exec;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.init.indexing.IndexingK3sInitNode;
import com.hayden.test_graph.commit_diff_context.init.indexing.ctx.IndexingK3sInit;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class IndexingIndexingK3SClusterDeployNode implements IndexingK3sInitNode {

    private final Assertions assertions;

    @Override
    public IndexingK3sInit exec(IndexingK3sInit ctx, MetaCtx metaCtx) {
        log.info("Starting K3s cluster initialization");
        log.info("Deployment namespaces configured: {}", ctx.getDeploymentConfig());

        if (ctx.deploymentConfig().isEmpty()) {
            assertions.assertThat(false).withFailMessage("Deployment namespaces are not configured!").isTrue();
            return ctx;
        }


        try {
            // Run the deployment orchestrator via Python uv
            // The actual orchestration would call the deploy_helm/__main__.py with appropriate args
            if(deployToK3sCluster(ctx)) {
                try {
                    ctx.markClusterReady();
                } catch (Exception e) {
                    ctx.markClusterReadyErr(e);
                }
            } else {
                ctx.markClusterReadyErr();
            }
            log.info("K3s cluster deployment completed successfully");
        } catch (Exception e) {
            ctx.markClusterReadyErr(e);
            log.error("K3s cluster deployment failed", e);
        }

        return ctx;
    }

    private boolean deployToK3sCluster(IndexingK3sInit ctx) {
        log.debug("Deploying to K3s cluster with config: {}", ctx.getClusterConfig());
        var dep = ctx.getDeploymentConfig();

        throw new RuntimeException("Forgot to run the helm command with values.yaml!");
    }
}
