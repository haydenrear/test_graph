package com.hayden.test_graph.multi_agent_ide.assert_nodes.nodes;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx.MultiAgentIdeAssertCtx;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepCtx;
import com.hayden.test_graph.multi_agent_ide.edges.DataDepToAssertEdge;
import com.hayden.test_graph.multi_agent_ide.edges.InitToAssertEdge;
import com.hayden.test_graph.thread.ResettableThread;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ResettableThread
public class MultiAgentIdeUiEventAssertNode implements MultiAgentIdeAssertNode {

    @Autowired
    private Assertions assertions;

    @Override
    public MultiAgentIdeAssertCtx exec(MultiAgentIdeAssertCtx c, MetaCtx h) {
        List<MultiAgentIdeDataDepCtx.UiEventObservation> uiEvents = c.getUiEvents();
        if (uiEvents.isEmpty()) {
            assertions.assertSoftly(false, "No UI events captured for assertions");
            return c;
        }

        List<MultiAgentIdeDataDepCtx.UiEventObservation> allEvents = c.getUiEvents();
        for (MultiAgentIdeAssertCtx.MultiAgentIdeAssertion assertion : c.getPendingAssertions()) {
            if (assertion instanceof MultiAgentIdeAssertCtx.EventAssertion eventAssertion) {
                boolean matched = allEvents.stream().anyMatch(event ->
                        matchesEventAssertion(eventAssertion, event, allEvents));
                String message = "Expected UI event %s for nodeType=%s"
                        .formatted(eventAssertion.eventType(), eventAssertion.nodeType());
                assertions.assertSoftly(matched, message);
                if (matched) {
                    c.markAssertionExecuted(assertion);
                } else {
                    c.markAssertionFailed(assertion);
                }
            }
        }

        return c;
    }

    @Override
    public List<Class<? extends MultiAgentIdeAssertNode>> dependsOn() {
        return List.of(DataDepToAssertEdge.class, InitToAssertEdge.class);
    }

    private boolean matchesEventAssertion(
            MultiAgentIdeAssertCtx.EventAssertion expected,
            MultiAgentIdeDataDepCtx.UiEventObservation actual,
            List<MultiAgentIdeDataDepCtx.UiEventObservation> allEvents
    ) {
        if (!expected.eventType().equals(actual.type())) {
            return false;
        }
        if (expected.nodeId() != null && !expected.nodeId().isBlank()) {
            if (actual.nodeId() == null || !expected.nodeId().equals(actual.nodeId())) {
                return false;
            }
        }
        if (expected.nodeType() != null && !expected.nodeType().isBlank()) {
            String nodeType = extractNodeType(actual, allEvents);
            return expected.nodeType().equals(nodeType);
        }
        return true;
    }

    private String extractNodeType(
            MultiAgentIdeDataDepCtx.UiEventObservation actual,
            List<MultiAgentIdeDataDepCtx.UiEventObservation> allEvents
    ) {
        Map<String, Object> rawEvent = actual.rawEvent();
        if (rawEvent != null) {
            Object nodeType = rawEvent.get("nodeType");
            if (nodeType != null) {
                return nodeType.toString();
            }
            if (rawEvent.containsKey("worktreeType")) {
                return "WORKTREE";
            }
        }
        String nodeId = actual.nodeId();
        if (nodeId == null) {
            return null;
        }
        return allEvents.stream()
                .map(MultiAgentIdeDataDepCtx.UiEventObservation::rawEvent)
                .filter(event -> event != null && nodeId.equals(event.get("nodeId")))
                .map(event -> event.get("nodeType"))
                .filter(type -> type != null && !type.toString().isBlank())
                .map(Object::toString)
                .findFirst()
                .orElse(null);
    }
}
