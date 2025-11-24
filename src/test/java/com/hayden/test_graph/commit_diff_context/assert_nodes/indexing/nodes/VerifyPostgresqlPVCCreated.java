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
 * Verifies that a PersistentVolumeClaim (PVC) for PostgreSQL has been created in the indexing namespace.
 * This assertion checks that the indexing job has proper persistent storage for the database via kubectl.
 * Skips verification if persister deployment is not enabled.
 */
@Component
@ResettableThread
public class VerifyPostgresqlPVCCreated implements CommitDiffContextIndexingAssertNode {

    @Autowired
    private Assertions assertions;

    @Autowired
    private KubernetesVerifier kubernetesVerifier;

    @Override
    @Idempotent(returnArg = 0)
    public CommitDiffContextIndexingAssertCtx exec(CommitDiffContextIndexingAssertCtx c, MetaCtx h) {
        // Skip assertion if persister is not enabled for deployment
        if (!c.isPersisterEnabled()) {
            return c;
        }
        
        String kubeconfig = c.getKubeConfigPath();
        assertions.assertThat(kubeconfig)
                .as("KubeConfig path should be configured")
                .isNotNull();
        
        var persisterConfig = c.getPersisterConfig();
        assertions.assertThat(persisterConfig)
                .as("Persister configuration should be present")
                .isNotNull();
        
        String indexingNamespace = persisterConfig.namespace();
        assertions.assertThat(indexingNamespace)
                .as("Indexing namespace should be configured for PVC creation")
                .isNotBlank();
        
        // Verify indexing namespace exists
        boolean namespaceExists = kubernetesVerifier.namespaceExists(indexingNamespace, kubeconfig);
        assertions.assertThat(namespaceExists)
                .as("Indexing namespace " + indexingNamespace + " should exist in cluster")
                .isTrue();
        
        // Verify PostgreSQL PVC exists and is bound
        boolean pvcExists = kubernetesVerifier.pvcExists("postgres-pvc", indexingNamespace, kubeconfig);
        assertions.assertThat(pvcExists)
                .as("PostgreSQL PVC 'postgres-pvc' should be created and bound in namespace " + indexingNamespace)
                .isTrue();
        
        return c;
    }
}
