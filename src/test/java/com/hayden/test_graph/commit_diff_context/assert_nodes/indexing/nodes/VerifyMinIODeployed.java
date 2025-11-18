package com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.nodes;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.CommitDiffContextIndexingAssertNode;
import com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.ctx.CommitDiffContextIndexingAssertCtx;
import com.hayden.test_graph.init.k3s.KubernetesVerifier;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Verifies that MinIO is deployed in the K3s cluster.
 * This assertion checks that MinIO pods are running in the configured namespace via kubectl.
 */
@Component
@ResettableThread
public class VerifyMinIODeployed implements CommitDiffContextIndexingAssertNode {

    @Autowired
    private Assertions assertions;

    @Autowired
    private KubernetesVerifier kubernetesVerifier;

    @Override
    public Class<? extends CommitDiffContextIndexingAssertCtx> clzz() {
        return CommitDiffContextIndexingAssertCtx.class;
    }

    @Override
    @Idempotent(returnArg = 0)
    public CommitDiffContextIndexingAssertCtx exec(CommitDiffContextIndexingAssertCtx c, MetaCtx h) {
        // Skip assertion if MinIO is not enabled for deployment
        if (!c.isMinIOEnabled()) {
            return c;
        }
        
        String kubeconfig = c.getKubeConfigPath();
        assertions.assertThat(kubeconfig)
                .as("KubeConfig path should be configured")
                .isNotNull();
        
        var minioConfig = c.getMinioConfig();
        assertions.assertThat(minioConfig)
                .as("MinIO configuration should be present")
                .isNotNull();
        
        String minioNamespace = minioConfig.namespace();
        assertions.assertThat(minioNamespace)
                .as("MinIO namespace should be configured")
                .isNotBlank();
        
        // Verify MinIO namespace exists
        boolean namespaceExists = kubernetesVerifier.namespaceExists(minioNamespace, kubeconfig);
        assertions.assertThat(namespaceExists)
                .as("MinIO namespace " + minioNamespace + " should exist in cluster")
                .isTrue();
        
        // Verify MinIO deployment is ready
        boolean minioReady = kubernetesVerifier.deploymentReady("minio", minioNamespace, kubeconfig);
        assertions.assertThat(minioReady)
                .as("MinIO deployment should be ready in namespace " + minioNamespace)
                .isTrue();
        
        return c;
    }
}
