package com.hayden.test_graph.multi_agent_ide.init.mountebank;

import com.hayden.test_graph.init.mountebank.ctx.MbInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.stereotype.Component;

/**
 * Mountebank initialization for multi-agent-ide.
 * Provides mock LangChain4j responses and other external service mocking.
 */
@Component
@ResettableThread
public interface MultiAgentIdeMbInit extends MbInitCtx {
}
