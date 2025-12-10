package com.hayden.test_graph.multi_agent_ide.init.mountebank.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.mountebank.ctx.MbInitCtx;
import com.hayden.test_graph.multi_agent_ide.init.mountebank.nodes.MultiAgentIdeMbInitNode;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.mbtest.javabank.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mountebank context for multi-agent-ide.
 * Handles mocking of LangChain4j responses and other external services.
 */
@Component
@ResettableThread
@RequiredArgsConstructor
public class MultiAgentIdeMbInitCtx implements MbInitCtx {

    public record LangChain4jMockResponse(
            String responseType,  // "planning", "codegen", "review", "merge", "spec_validation", etc.
            String filePath,      // classpath path to response JSON
            String endpoint,      // HTTP endpoint path
            int port             // Mock server port
    ) {}

    public record MockResponses(List<LangChain4jMockResponse> responses) {
        public MockResponses() {
            this(new ArrayList<>());
        }
    }

    private Client client;
    private final ContextValue<MockResponses> mockResponses = ContextValue.empty();
    private final ContextValue<MultiAgentIdeMbInitBubbleCtx> bubbleUnderlying = ContextValue.empty();

    public MultiAgentIdeMbInitCtx() {
        this.mockResponses.swap(new MockResponses());
    }

    public void addMockResponse(LangChain4jMockResponse response) {
        MockResponses current = mockResponses.get();
        if (current == null) {
            current = new MockResponses();
            mockResponses.swap(current);
        }
        current.responses.add(response);
    }

    public void addMockResponse(String responseType, String filePath, String endpoint, int port) {
        addMockResponse(new LangChain4jMockResponse(responseType, filePath, endpoint, port));
    }

    public MockResponses getMockResponses() {
        return mockResponses.get();
    }

    @Override
    public Client client() {
        return this.client;
    }

    @Override
    @Autowired
    public void setClient(Client client) {
        this.client = client;
    }

    @Autowired
    public void setBubble(MultiAgentIdeMbInitBubbleCtx bubble) {
        this.bubbleUnderlying.swap(bubble);
    }

    public MultiAgentIdeMbInitBubbleCtx bubble() {
        return this.bubbleUnderlying.get();
    }

    @Override
    public Class<MultiAgentIdeMbInitBubbleCtx> bubbleClazz() {
        return MultiAgentIdeMbInitBubbleCtx.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof MultiAgentIdeMbInitNode;
    }
}
