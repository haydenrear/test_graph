package com.hayden.test_graph.init.k3s;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import com.hayden.test_graph.init.k3s.config.K3sInitConfigProps;
import com.hayden.utilitymodule.concurrent.OnceCell;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class K3sService {

    private final ConcurrentHashMap<String, ApiClient> clients = new ConcurrentHashMap<>();

    public ApiClient getCreateApiClient(String kubeConfig) {
        return clients.compute(kubeConfig, (key, prev) ->{
            if (prev == null) {
                try {
                    prev = createApiClient(kubeConfig);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return prev;
        });
    }

    private ApiClient createApiClient(String kubeconfig) throws IOException {
        if (kubeconfig != null && !kubeconfig.isEmpty()) {
            return Config.fromConfig(kubeconfig);
        } else {
            return Config.defaultClient();
        }
    }

}
