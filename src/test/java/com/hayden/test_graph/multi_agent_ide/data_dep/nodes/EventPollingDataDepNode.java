package com.hayden.test_graph.multi_agent_ide.data_dep.nodes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Data dependency node for polling events from the subscription and queuing them.
 * This node waits for events within a specified timeout and adds them to the EventQueue.
 * The queue is then transferred to the assert context for validation.
 */
@Slf4j
@Component
@ResettableThread
public class EventPollingDataDepNode implements MultiAgentIdeDataDepNode {

    @Override
    public MultiAgentIdeDataDepCtx exec(MultiAgentIdeDataDepCtx ctx, MetaCtx h) {
        MultiAgentIdeDataDepCtx.EventSubscriptionConfig config = ctx.getEventSubscriptionConfig();

        if (config == null) {
            log.info("No event subscription configuration found, skipping event polling");
            return ctx;
        }

        if (!"sse".equalsIgnoreCase(config.subscriptionProtocol())) {
            log.info("Subscription protocol {} is not supported for polling", config.subscriptionProtocol());
            return ctx;
        }

        if (ctx.getOrchestrationRequests().isEmpty()) {
            log.warn("No orchestration requests configured, skipping SSE polling");
            return ctx;
        }

        log.info(
                "Starting SSE polling: endpoint={}, timeout={}ms",
                config.eventEndpoint(),
                config.subscriptionTimeoutMs()
        );

        AtomicBoolean stop = new AtomicBoolean(false);
        AtomicReference<HttpURLConnection> connectionRef = new AtomicReference<>();
        Thread reader = new Thread(
                () -> readSseStream(config.eventEndpoint(), ctx, stop, connectionRef),
                "multi-agent-ide-sse-reader"
        );
        reader.setDaemon(true);
        reader.start();

        return ctx;
    }

    private void readSseStream(
            String endpoint,
            MultiAgentIdeDataDepCtx observations,
            AtomicBoolean stop,
            AtomicReference<HttpURLConnection> connectionRef) {
        if (endpoint == null || endpoint.isBlank()) {
            log.warn("SSE endpoint not configured");
            return;
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(0);
            connectionRef.set(connection);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                StringBuilder data = new StringBuilder();
                while (!stop.get() && (line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        data.append(line.substring(5).trim());
                        continue;
                    }
                    if (line.isBlank()) {
                        flushSseData(data, observations);
                    }
                }
                flushSseData(data, observations);
            }
        } catch (IOException e) {
            if (!stop.get()) {
                log.error("Error reading SSE stream", e);
            }
        } finally {
            closeConnection(connection);
        }
    }

    private void flushSseData(StringBuilder data, MultiAgentIdeDataDepCtx observations) {
        if (data == null || data.isEmpty()) {
            return;
        }
        String payload = data.toString();
        data.setLength(0);
        if (payload.isBlank()) {
            return;
        }
        Map<String, Object> envelope = parseEnvelope(payload);
        if (envelope == null) {
            return;
        }
        MultiAgentIdeDataDepCtx.UiEventObservation observation = toObservation(envelope);
        if (observation != null) {
            observations.getEventQueue().enqueue(observation);
        }
    }

    private Map<String, Object> parseEnvelope(String payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(payload, new TypeReference<>() {});
        } catch (Exception e) {
            log.debug("Failed to parse SSE event payload", e);
            return null;
        }
    }

    private MultiAgentIdeDataDepCtx.UiEventObservation toObservation(Map<String, Object> envelope) {
        if (envelope == null) {
            return null;
        }
        String type = extractType(envelope);
        Map<String, Object> rawEvent = asMap(envelope.get("rawEvent"));
        Map<String, Object> payload = asMap(envelope.get("payload"));
        if (payload == null) {
            payload = asMap(envelope.get("value"));
        }
        if ("WORKTREE_CREATED".equals(type) && rawEvent != null && !rawEvent.containsKey("nodeType")) {
            rawEvent.put("nodeType", "WORKTREE");
        }
        String nodeId = extractNodeId(rawEvent);
        String id = extractId(rawEvent, type, nodeId, envelope);
        return MultiAgentIdeDataDepCtx.UiEventObservation.builder()
                .id(id)
                .type(type)
                .nodeId(nodeId)
                .rawEvent(rawEvent)
                .payload(payload)
                .build();
    }

    private String extractType(Map<String, Object> envelope) {
        Object type = envelope.get("type");
        if (type instanceof String text) {
            return text;
        }
        if (type instanceof Map<?, ?> map) {
            Object name = map.get("name");
            if (name != null) {
                return name.toString();
            }
        }
        Object name = envelope.get("name");
        if (name != null) {
            return name.toString();
        }
        Map<String, Object> rawEvent = asMap(envelope.get("rawEvent"));
        Object eventType = rawEvent != null ? rawEvent.get("eventType") : null;
        return eventType != null ? eventType.toString() : "UNKNOWN";
    }

    private String extractNodeId(Map<String, Object> rawEvent) {
        if (rawEvent == null) {
            return null;
        }
        Object nodeId = rawEvent.get("nodeId");
        if (nodeId != null) {
            return nodeId.toString();
        }
        Object associatedNodeId = rawEvent.get("associatedNodeId");
        if (associatedNodeId != null) {
            return associatedNodeId.toString();
        }
        Object branchedNodeId = rawEvent.get("branchedNodeId");
        if (branchedNodeId != null) {
            return branchedNodeId.toString();
        }
        Object originalNodeId = rawEvent.get("originalNodeId");
        if (originalNodeId != null) {
            return originalNodeId.toString();
        }
        Object orchestratorNodeId = rawEvent.get("orchestratorNodeId");
        if (orchestratorNodeId != null) {
            return orchestratorNodeId.toString();
        }
        Object reviewNodeId = rawEvent.get("reviewNodeId");
        if (reviewNodeId != null) {
            return reviewNodeId.toString();
        }
        return null;
    }

    private String extractId(
            Map<String, Object> rawEvent,
            String type,
            String nodeId,
            Map<String, Object> envelope) {
        if (rawEvent != null) {
            Object eventId = rawEvent.get("eventId");
            if (eventId != null) {
                return eventId.toString();
            }
        }
        Object timestamp = envelope.get("timestamp");
        String timeValue = timestamp != null ? timestamp.toString() : String.valueOf(System.nanoTime());
        return "%s-%s-%s".formatted(type != null ? type : "UNKNOWN", nodeId != null ? nodeId : "global", timeValue);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private void closeConnection(HttpURLConnection connection) {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception ignored) {
                // ignore shutdown errors
            }
        }
    }

}
