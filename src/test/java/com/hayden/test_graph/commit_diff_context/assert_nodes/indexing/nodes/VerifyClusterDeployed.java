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

import java.nio.file.Path;

/**
 * Verifies that the K3s cluster is properly deployed and accessible.
 * This assertion checks that the Kubernetes API is responding and the cluster is ready via kubectl.
 */
@Component
@ResettableThread
public class VerifyClusterDeployed implements CommitDiffContextIndexingAssertNode {

    @Autowired
    private Assertions assertions;

    @Autowired
    private KubernetesVerifier kubernetesVerifier;

    @Override
    @Idempotent(returnArg = 0)
    public CommitDiffContextIndexingAssertCtx exec(CommitDiffContextIndexingAssertCtx c, MetaCtx h) {
        if (!c.isClusterEnabled())
            return c;

        c.getDataDepContext().optional()
                .ifPresent(dataDep -> {
                    dataDep.deployment().res()
                            .ifPresent(deployment -> {
                                String kubeconfig = deployment.kubeConfigPath()
                                        .map(Path::toString)
                                        .orElse(null);
                                
                                assertions.assertThat(kubeconfig)
                                        .as("KubeConfig path should be configured")
                                        .isNotNull();
                                
                                // Verify cluster is accessible by checking if we can read the default namespace
                                boolean clusterAccessible = kubernetesVerifier.namespaceExists("default", kubeconfig);
                                assertions.assertThat(clusterAccessible)
                                        .as("K3s cluster should be deployed and accessible via Kubernetes API")
                                        .isTrue();
                            });
                });
        return c;
    }
}
