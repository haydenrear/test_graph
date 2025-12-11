package com.hayden.test_graph.multi_agent_ide.init.mountebank.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.mountebank.ctx.MbInitCtx;
import com.hayden.test_graph.multi_agent_ide.init.mountebank.nodes.MultiAgentIdeMbInitNode;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.mbtest.javabank.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mountebank context for multi-agent-ide.
 * Handles mocking of LangChain4j responses and other external services.
 * 
 * Uses a single consolidated JSON payload file with matchers for multiple agent turns.
 * Structure: { impostors: [ { port, protocol, stubs: [ { predicates: [...], responses: [...] } ] } ] }
 */
@Component
@ResettableThread
@Slf4j
public class MultiAgentIdeMbInitCtx implements MbInitCtx {

    /**
     * Consolidated configuration for all mock responses.
     * Contains multiple impostors, each with their own stubs.
     */
    public record MockResponses(
            Map<String, Path> impostorsByName,
            String sourceJsonPath
    ) {
        public MockResponses() {
            this(new LinkedHashMap<>(), null);
        }
        
        public MockResponses(String sourceJsonPath) {
            this(new LinkedHashMap<>(), sourceJsonPath);
        }
    }

    private Client client;

    @Getter
    private final MockResponses mockResponses;

    private final ContextValue<MultiAgentIdeMbInitBubbleCtx> bubbleUnderlying = ContextValue.empty();

    public MultiAgentIdeMbInitCtx() {
        this.mockResponses = new MockResponses();
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

    public void registerImposterFile(String mocks) {
        this.mockResponses.impostorsByName.put(mocks, Paths.get(mocks));
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
