package com.hayden.test_graph.commit_diff_context.init.llm_validation.ctx;

import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInitBubble;
import com.hayden.test_graph.commit_diff_context.init.llm_validation.ValidateLlmInitBubbleNode;
import com.hayden.test_graph.commit_diff_context.init.mountebank.commitdiff.ctx.CdMbInitBubbleCtx;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpBubble;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.docker.ctx.DockerInitBubbleCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
@ResettableThread
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class ValidateLlmBubble implements InitBubble {


    private final ContextValue<ValidateLlmInit> repoInit;


    public ValidateLlmBubble() {
        this(ContextValue.empty());
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof ValidateLlmInitBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext<InitBubble>>> dependsOn() {
        return List.of(DockerInitBubbleCtx.class, CommitDiffInitBubble.class, CdMbInitBubbleCtx.class, RepoOpBubble.class);
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(ValidateLlmInit.class);
    }
}
