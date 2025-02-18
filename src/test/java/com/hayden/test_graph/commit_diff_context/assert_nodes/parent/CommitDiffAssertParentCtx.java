package com.hayden.test_graph.commit_diff_context.assert_nodes.parent;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssert;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Example parent context - share data across multiple types of contexts - data extensions through delegations.
 */
@Component
@ResettableThread
@RequiredArgsConstructor
@Getter
public class CommitDiffAssertParentCtx implements CommitDiffAssert {

    private final ContextValue<RepoOpInit.GraphQlQueries> graphQlQueries;
    private final ContextValue<Boolean> validated;
    private final ContextValue<RepoOpInit> repoInitData;

    private CommitDiffCtxParentBubble commitDiffAssertBubble;

    public CommitDiffAssertParentCtx() {
        this(ContextValue.empty(), ContextValue.empty(), ContextValue.empty());
    }

    @Autowired
    @ResettableThread
    public void setBubble(CommitDiffCtxParentBubble commitDiffAssertBubble) {
        this.commitDiffAssertBubble = commitDiffAssertBubble;
    }

    @Override
    public CommitDiffCtxParentBubble bubble() {
        return commitDiffAssertBubble;
    }

    @Override
    public Class<? extends AssertBubble> bubbleClazz() {
        return CommitDiffCtxParentBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffCtxParentAssertNode;
    }

    public Optional<RepoOpInit.RepositoryData> repoUrl() {
        return this.repoInitData.optional()
                .flatMap(r -> Optional.ofNullable(r.repoDataOrNull()));
    }

    @Override
    public boolean isLeafNode() {
        return false;
    }
}
