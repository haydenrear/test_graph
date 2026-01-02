package com.hayden.test_graph.multi_agent_ide.assert_nodes.nodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx.MultiAgentIdeAssertCtx;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepCtx;
import com.hayden.test_graph.multi_agent_ide.edges.MultiAgentIdeDataDepToAssertEdge;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ResettableThread
public class MultiAgentSseGoalRequestNode implements MultiAgentIdeAssertNode {

    @Autowired
    private Assertions assertions;
    @Autowired
    private MultiAgentIdeDataDepCtx ctx;

    @Override
    public MultiAgentIdeAssertCtx exec(MultiAgentIdeAssertCtx c, MetaCtx h) {
        assertions.assertSoftly(CollectionUtils.isNotEmpty(ctx.getOrchestrationRequests()),
                "Did not contain any orchestration requests to run.");

        for (MultiAgentIdeDataDepCtx.OrchestrationRequestConfig request : ctx.getOrchestrationRequests()) {
            submitOrchestrationRequest(request);
        }

        return c;
    }

    @Override
    public List<Class<? extends MultiAgentIdeAssertNode>> dependsOn() {
        return List.of(MultiAgentIdeDataDepToAssertEdge.class);
    }

    private void submitOrchestrationRequest(MultiAgentIdeDataDepCtx.OrchestrationRequestConfig request) {
        if (request.goal() == null || request.goal().isBlank()) {
            throw new IllegalArgumentException("Goal is required for orchestration request");
        }
        if (request.repositoryUrl() == null || request.repositoryUrl().isBlank()) {
            throw new IllegalArgumentException("Repository URL is required for orchestration request");
        }
        String baseUrl = request.baseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8080";
        }
        String endpoint = baseUrl.endsWith("/")
                ? baseUrl + "api/orchestrator/start"
                : baseUrl + "/api/orchestrator/start";

        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("goal", request.goal());
        payload.put("repositoryUrl", request.repositoryUrl());
        if (request.baseBranch() != null) {
            payload.put("baseBranch", request.baseBranch());
        }
        if (request.title() != null) {
            payload.put("title", request.title());
        }
        if (request.nodeId() != null) {
            payload.put("nodeId", request.nodeId());
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            ObjectMapper mapper = new ObjectMapper();
            byte[] body = mapper.writeValueAsBytes(payload);
            connection.getOutputStream().write(body);
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IOException("Unexpected response status " + status);
            }
            connection.disconnect();
        } catch (IOException e) {
            assertions.assertSoftly(false, "Failed to submit orchestration request %s".formatted(e));
        }
    }

}
