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
 * Verifies that Kafka is deployed in the K3s cluster.
 * This assertion checks that Kafka broker pods are running in the configured namespace via kubectl.
 * Skips verification if Kafka deployment is not enabled.
 */
@Component
@ResettableThread
public class VerifyKafkaDeployed implements CommitDiffContextIndexingAssertNode {

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
        // Skip assertion if Kafka is not enabled for deployment
        if (!c.isKafkaEnabled()) {
            return c;
        }
        
        String kubeconfig = c.getKubeConfigPath();
        assertions.assertThat(kubeconfig)
                .as("KubeConfig path should be configured")
                .isNotNull();
        
        var kafkaConfig = c.getKafkaConfig();
        assertions.assertThat(kafkaConfig)
                .as("Kafka configuration should be present")
                .isNotNull();
        
        String kafkaNamespace = kafkaConfig.namespace();
        assertions.assertThat(kafkaNamespace)
                .as("Kafka namespace should be configured")
                .isNotBlank();
        
        // Verify Kafka namespace exists
        boolean namespaceExists = kubernetesVerifier.namespaceExists(kafkaNamespace, kubeconfig);
        assertions.assertThat(namespaceExists)
                .as("Kafka namespace " + kafkaNamespace + " should exist in cluster")
                .isTrue();
        
        // Verify Kafka StatefulSet is ready
        boolean kafkaReady = kubernetesVerifier.statefulSetReady("kafka", kafkaNamespace, kubeconfig);
        assertions.assertThat(kafkaReady)
                .as("Kafka StatefulSet should be ready in namespace " + kafkaNamespace)
                .isTrue();
        
        return c;
    }
}
