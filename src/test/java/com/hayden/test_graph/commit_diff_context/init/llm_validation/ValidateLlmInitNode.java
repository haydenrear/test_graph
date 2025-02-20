package com.hayden.test_graph.commit_diff_context.init.llm_validation;

import com.hayden.test_graph.commit_diff_context.init.llm_validation.ctx.ValidateLlmInit;
import com.hayden.test_graph.init.exec.single.InitNode;

public interface ValidateLlmInitNode extends InitNode<ValidateLlmInit> {

    default Class<? extends ValidateLlmInit> clzz() {
        return ValidateLlmInit.class;
    }
}
