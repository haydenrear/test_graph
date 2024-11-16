package com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssert;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssertNode;
import com.hayden.test_graph.commit_diff_context.assert_nodes.parent.CommitDiffAssertParentCtx;
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
public class RepoOpAssertCtx implements CommitDiffAssert {

    public record RepoOpAssertionDescriptor(String branchToBeAdded) {}

    private final ContextValue<RepoOpAssertionDescriptor> repositoryAssertionDescriptor;

    private RepoOpAssertBubble commitDiffAssertBubble;

    private ContextValue<CommitDiffAssertParentCtx> parent;


    @Autowired
    public void setRepoOpBubble(RepoOpAssertBubble commitDiffAssertBubble) {
        this.commitDiffAssertBubble = commitDiffAssertBubble;
    }

    public RepoOpAssertCtx() {
        this(ContextValue.empty());
    }

    @Override
    public RepoOpAssertBubble bubble() {
        return commitDiffAssertBubble;
    }

    @Override
    public Class<? extends AssertBubble> bubbleClazz() {
        return RepoOpAssertBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffAssertNode;
    }

    public ContextValue<RepoOpInit.RepositoryData> repoUrl() {
        return this.parent
                .res().map(CommitDiffAssertParentCtx::repoUrl)
                .orElseRes(ContextValue.empty());
    }

    @Override
    public Optional<Class<? extends TestGraphContext>> parentTy() {
        return Optional.of(CommitDiffAssertParentCtx.class) ;
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
