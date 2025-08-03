package com.hayden.test_graph.commit_diff_context.assert_nodes.next_commit;

import com.hayden.commitdiffmodel.codegen.types.NextCommit;
import com.hayden.commitdiffmodel.validation.entity.CommitDiffContextCommitVersion;
import com.hayden.proto.prototyped.datasources.ai.modelserver.client.ModelServerValidationAiClient;
import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssert;
import com.hayden.test_graph.commit_diff_context.assert_nodes.parent.CommitDiffAssertParentCtx;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
@ResettableThread
@RequiredArgsConstructor
@Getter
public class NextCommitAssert implements CommitDiffAssert {

    public record NextCommitMetadata(NextCommit nc) {}

    public record NextCommitLlmValidation(ModelServerValidationAiClient.ModelServerValidationResponse response) {}

    private final ContextValue<NextCommitMetadata> nextCommitInfo;

    @Getter
    private final ContextValue<CommitDiffContextCommitVersion.AssertedGitDiffs> actualCommitInfo;

    @Getter
    private final ContextValue<NextCommitLlmValidation> validationResponse;

    private NextCommitBubble nextCommitBubble;

    private ContextValue<CommitDiffAssertParentCtx> parent;



    public NextCommitAssert() {
        this(ContextValue.empty(), ContextValue.empty(),ContextValue.empty());
    }

    @Autowired
    @ResettableThread
    public void setNextCommitBubble(NextCommitBubble commitDiffAssertBubble) {
        this.nextCommitBubble = commitDiffAssertBubble;
    }

    @Override
    public NextCommitBubble bubble() {
        return nextCommitBubble;
    }

    @Override
    public Class<? extends AssertBubble> bubbleClazz() {
        return NextCommitBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof NextCommitAssertNode;
    }

    @Override
    public Optional<Class<? extends TestGraphContext>> parentTy() {
        return Optional.of(CommitDiffAssertParentCtx.class);
    }

    @Override
    public void doSet(TestGraphContext context) {
        if (context instanceof CommitDiffAssertParentCtx c) {
            assert this.parent == null || this.parent.isEmpty();
            this.parent = ContextValue.ofExisting(c);
        }
    }

    @Override
    public ContextValue<CommitDiffAssertParentCtx> parent() {
        return parent;
    }
}
