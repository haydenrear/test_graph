package com.hayden.test_graph.commit_diff_context.init.llm_validation.ctx;

import com.hayden.commitdiffmodel.codegen.types.*;
import com.hayden.commitdiffmodel.comittdiff.ParseDiff;
import com.hayden.commitdiffmodel.git.RepositoryHolder;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.init.llm_validation.ValidateLlmInitNode;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.commit_diff_context.service.CallGraphQlQueryArgs;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.sort.GraphSort;
import com.hayden.utilitymodule.stream.StreamUtil;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

@Component
@ResettableThread
@RequiredArgsConstructor
public class ValidateLlmInit implements InitCtx {

    public record LlmValidationCommitData(List<ParseDiff.GitDiffResult> diffs, String commitMessage) {}

    private Assertions assertions;


    private final ContextValue<ValidateLlmBubble> bubbleUnderlying;
    private final ContextValue<RepoOpInit> repoOpInit;
    @Getter
    private final ContextValue<LlmValidationCommitData> llmValidationData;

    public ValidateLlmInit() {
        this(ContextValue.empty(), ContextValue.empty(), ContextValue.empty());
    }

    @Autowired
    @ResettableThread
    public void setBubble(ValidateLlmBubble bubble, RepoOpInit repoOpInit, Assertions assertions) {
        this.bubbleUnderlying.swap(bubble);
        this.bubbleUnderlying.res().one().get().getRepoInit().swap(this);
        this.repoOpInit.swap(repoOpInit);
        this.assertions = assertions;
    }

    public RepoOpInit getRepoOpInit() {
        return this.repoOpInit.res().get();
    }

    @Override
    public ValidateLlmBubble bubble() {
        return this.bubbleUnderlying.res().one().get();
    }

    @Override
    public Class<ValidateLlmBubble> bubbleClazz() {
        return ValidateLlmBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof ValidateLlmInitNode;
    }

    public ContextValue<ValidateLlmBubble> bubbleUnderlying() {
        return bubbleUnderlying;
    }


}
