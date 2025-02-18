package com.hayden.test_graph.commit_diff_context.assert_nodes.codegen;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssert;
import com.hayden.test_graph.commit_diff_context.assert_nodes.parent.CommitDiffAssertParentCtx;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
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
public class Codegen implements CommitDiffAssert {

    private final ContextValue<RepoOpInit.UserCodeData> userCode;

    private CodegenBubble commitDiffAssertBubble;

    private ContextValue<CommitDiffAssertParentCtx> parent;


    public Codegen() {
        this(ContextValue.empty());
    }

    @Autowired
    @ResettableThread
    public void setCodegenBubble(CodegenBubble commitDiffAssertBubble) {
        this.commitDiffAssertBubble = commitDiffAssertBubble;
    }

    @Override
    public CodegenBubble bubble() {
        return commitDiffAssertBubble;
    }

    @Override
    public Class<? extends AssertBubble> bubbleClazz() {
        return CodegenBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CodegenAssertNode;
    }

    @Override
    public Optional<Class<? extends TestGraphContext>> parentTy() {
        return Optional.of(CommitDiffAssertParentCtx.class);
    }

    public Optional<RepoOpInit.RepositoryData> repoUrl() {
        return this.parent
                .res()
                .map(CommitDiffAssertParentCtx::repoUrl)
                .one()
                .orElseRes(Optional.empty());
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
