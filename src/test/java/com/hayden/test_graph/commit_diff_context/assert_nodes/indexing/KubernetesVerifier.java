package com.hayden.test_graph.commit_diff_context.assert_nodes.indexing;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for verifying Kubernetes deployments and resources.
 * Uses the official Kubernetes Java client to check if resources are deployed and running.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KubernetesVerifier {

    /**
     * Creates an API client from the kubeconfig file.
     *
     * @param kubeconfig path to kubeconfig file
     * @return ApiClient instance
     * @throws IOException if kubeconfig cannot be read
     */
    private ApiClient createApiClient(String kubeconfig) throws IOException {
        if (kubeconfig != null && !kubeconfig.isEmpty()) {
            return Config.fromConfig(kubeconfig);
        } else {
            return Config.defaultClient();
        }
    }

    /**
     * Verifies that a namespace exists in the cluster.
     *
     * @param namespace the namespace name
     * @param kubeconfig path to kubeconfig file
     * @return true if the namespace exists, false otherwise
     */
    public boolean namespaceExists(String namespace, String kubeconfig) {
        try {
            ApiClient client = createApiClient(kubeconfig);
            CoreV1Api coreApi = new CoreV1Api(client);
            coreApi.readNamespace(namespace);
            return true;
        } catch (IOException e) {
            log.error("Failed to create API client: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifies that a deployment exists and is ready in the specified namespace.
     *
     * @param deploymentName the name of the deployment
     * @param namespace the namespace
     * @param kubeconfig path to kubeconfig file
     * @return true if deployment is ready, false otherwise
     */
    public boolean deploymentReady(String deploymentName, String namespace, String kubeconfig) {
        try {
            ApiClient client = createApiClient(kubeconfig);
            AppsV1Api appsApi = new AppsV1Api(client);
            var deployment = appsApi.readNamespacedDeployment(deploymentName, namespace)
                    .execute();

            if (deployment.getStatus() == null) {
                return false;
            }

            Integer replicas = deployment.getStatus().getReplicas();
            Integer readyReplicas = deployment.getStatus().getReadyReplicas();

            return replicas > 0 && replicas.equals(readyReplicas);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                log.warn("Deployment {}/{} not found", namespace, deploymentName);
                return false;
            }
            log.error("Failed to verify deployment {}/{}: {}", namespace, deploymentName, e.getMessage());
            return false;
        } catch (IOException e) {
            log.error("Failed to create API client: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifies that pods are running in a namespace with a specific label.
     *
     * @param namespace the namespace
     * @param labelSelector label selector for filtering pods
     * @param kubeconfig path to kubeconfig file
     * @return true if at least one running pod is found, false otherwise
     */
    public boolean podsRunning(String namespace, String labelSelector, String kubeconfig) {
        try {
            ApiClient client = createApiClient(kubeconfig);
            CoreV1Api coreApi = new CoreV1Api(client);
            var podList = coreApi.listNamespacedPod(namespace).execute();

            if (podList.getItems().isEmpty()) {
                log.warn("No pods found in {}/{}", namespace, labelSelector);
                return false;
            }

            return podList.getItems().stream()
                    .anyMatch(pod -> pod.getStatus() != null &&
                            "Running".equals(pod.getStatus().getPhase()));
        } catch (ApiException e) {
            log.error("Failed to verify pods in {}: {}", namespace, e.getMessage());
            return false;
        } catch (IOException e) {
            log.error("Failed to create API client: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifies that a PersistentVolumeClaim (PVC) exists in the specified namespace.
     *
     * @param pvcName the name of the PVC
     * @param namespace the namespace
     * @param kubeconfig path to kubeconfig file
     * @return true if PVC exists and is bound, false otherwise
     */
    public boolean pvcExists(String pvcName, String namespace, String kubeconfig) {
        try {
            ApiClient client = createApiClient(kubeconfig);
            CoreV1Api coreApi = new CoreV1Api(client);
            var pvc = coreApi.readNamespacedPersistentVolumeClaim(pvcName, namespace).execute();

            if (pvc.getStatus() == null) {
                return false;
            }

            return "Bound".equals(pvc.getStatus().getPhase());
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                log.warn("PVC {}/{} not found", namespace, pvcName);
                return false;
            }
            log.error("Failed to verify PVC {}/{}: {}", namespace, pvcName, e.getMessage());
            return false;
        } catch (IOException e) {
            log.error("Failed to create API client: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifies that a StatefulSet exists and is ready in the specified namespace.
     *
     * @param statefulSetName the name of the StatefulSet
     * @param namespace the namespace
     * @param kubeconfig path to kubeconfig file
     * @return true if StatefulSet is ready, false otherwise
     */
    public boolean statefulSetReady(String statefulSetName, String namespace, String kubeconfig) {
        try {
            ApiClient client = createApiClient(kubeconfig);
            AppsV1Api appsApi = new AppsV1Api(client);
            var statefulSet = appsApi.readNamespacedStatefulSet(statefulSetName, namespace).execute();

            if (statefulSet.getStatus() == null) {
                return false;
            }

            Integer replicas = statefulSet.getStatus().getReplicas();
            Integer readyReplicas = statefulSet.getStatus().getReadyReplicas();

            return replicas > 0 && replicas.equals(readyReplicas);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                log.warn("StatefulSet {}/{} not found", namespace, statefulSetName);
                return false;
            }
            log.error("Failed to verify StatefulSet {}/{}: {}", namespace, statefulSetName, e.getMessage());
            return false;
        } catch (IOException e) {
            log.error("Failed to create API client: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gets all deployed services in a namespace.
     *
     * @param namespace the namespace
     * @param kubeconfig path to kubeconfig file
     * @return set of service names deployed in the namespace
     */
    public Set<String> getDeployedServices(String namespace, String kubeconfig) {
        try {
            ApiClient client = createApiClient(kubeconfig);
            CoreV1Api coreApi = new CoreV1Api(client);
            var serviceList = coreApi.listNamespacedService(namespace).execute();

            return serviceList.getItems().stream()
                    .map(service -> service.getMetadata().getName())
                    .collect(Collectors.toSet());
        } catch (ApiException e) {
            log.error("Failed to get deployed services in namespace {}: {}", namespace, e.getMessage());
            return Collections.emptySet();
        } catch (IOException e) {
            log.error("Failed to create API client: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Gets all deployed deployments in a namespace.
     *
     * @param namespace the namespace
     * @param kubeconfig path to kubeconfig file
     * @return set of deployment names deployed in the namespace
     */
    public Set<String> getDeployedDeployments(String namespace, String kubeconfig) {
        try {
            ApiClient client = createApiClient(kubeconfig);
            AppsV1Api appsApi = new AppsV1Api(client);
            var deploymentList = appsApi.listNamespacedDeployment(namespace).execute();

            return deploymentList.getItems().stream()
                    .flatMap(d -> Stream.ofNullable(d.getMetadata()))
                    .map(V1ObjectMeta::getName)
                    .collect(Collectors.toSet());
        } catch (ApiException e) {
            log.error("Failed to get deployed deployments in namespace {}: {}", namespace, e.getMessage());
            return Collections.emptySet();
        } catch (IOException e) {
            log.error("Failed to create API client: {}", e.getMessage());
            return Collections.emptySet();
        }
    }
}
