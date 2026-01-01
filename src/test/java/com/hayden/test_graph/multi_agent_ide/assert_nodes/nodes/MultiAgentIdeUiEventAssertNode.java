package com.hayden.test_graph.multi_agent_ide.assert_nodes.nodes;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.multi_agent_ide.assert_nodes.ctx.MultiAgentIdeAssertCtx;
import com.hayden.test_graph.multi_agent_ide.data_dep.ctx.MultiAgentIdeDataDepCtx;
import com.hayden.test_graph.thread.ResettableThread;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.awaitility.Awaitility.await;

@Slf4j
@Component
@ResettableThread
public class MultiAgentIdeUiEventAssertNode implements MultiAgentIdeAssertNode {

    @Autowired
    private Assertions assertions;

    @Override
    public MultiAgentIdeAssertCtx exec(MultiAgentIdeAssertCtx c, MetaCtx h) {

        await().atMost(c.getConfig().maxWait()).until(() -> {
            List<MultiAgentIdeDataDepCtx.UiEventObservation> allEvents = c.getUiEvents();
            for (MultiAgentIdeAssertCtx.MultiAgentIdeAssertion toAssert : c.getPendingAssertions()) {
                boolean matched = allEvents.stream().anyMatch(event ->
                        matchesEventAssertion(toAssert, event, allEvents));
                String message = "Expected UI event %s"
                        .formatted(toAssert);
                if (matched) {
                    assertions.assertSoftly(true, message);
                    c.markAssertionExecuted(toAssert);
                }
            }

            return c.getPendingAssertions().isEmpty();
        });


        for (MultiAgentIdeAssertCtx.MultiAgentIdeAssertion assertion : c.getPendingAssertions()) {
            assertions.assertSoftly(false, "%s failed".formatted(assertion));
            c.markAssertionFailed(assertion);
        }

        return c;
    }

    @Override
    public List<Class<? extends MultiAgentIdeAssertNode>> dependsOn() {
        return List.of(MultiAgentGoalRequestNode.class);
    }

    private boolean matchesEventAssertion(
            MultiAgentIdeAssertCtx.MultiAgentIdeAssertion toAssert,
            MultiAgentIdeDataDepCtx.UiEventObservation actual,
            List<MultiAgentIdeDataDepCtx.UiEventObservation> allEvents
    ) {
        return switch (toAssert) {
            case MultiAgentIdeAssertCtx.EventAssertion  expected -> {
                if (!expected.eventType().equals(actual.type())) {
                    yield false;
                }
                if (expected.nodeId() != null && !expected.nodeId().isBlank()) {
                    if (actual.nodeId() == null || !expected.nodeId().equals(actual.nodeId())) {
                        yield false;
                    }
                }
                if (expected.nodeType() != null && !expected.nodeType().isBlank()) {
                    String nodeType = extractNodeType(actual, allEvents);
                    yield expected.nodeType().equals(nodeType);
                }
                yield true;
            }
            default -> false;
        };
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
