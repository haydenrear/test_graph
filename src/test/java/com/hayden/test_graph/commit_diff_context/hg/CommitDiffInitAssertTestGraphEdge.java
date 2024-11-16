package com.hayden.test_graph.commit_diff_context.hg;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.commit_diff_context.assert_nodes.parent.CommitDiffAssertParentCtx;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInitBubble;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpBubble;
import com.hayden.test_graph.graph.edge.PreExecTestGraphEdge;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.ctx.MetaProgCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
@ResettableThread
public class CommitDiffInitAssertTestGraphEdge implements PreExecTestGraphEdge<CommitDiffAssertParentCtx, AssertBubble> {

    @Override
    public CommitDiffAssertParentCtx edge(CommitDiffAssertParentCtx first, MetaCtx s) {
        if (s instanceof MetaProgCtx second) {
            var i = second.retrieveBubbled(RepoOpBubble.class)
                    .toList();

            if (i.size() != 1)  {
                throw new RuntimeException("Failed to find commit diff init bubble: %s.".formatted(i));
            }

            i.stream().findAny()
                    .flatMap(c -> c.repositoryData().optional())
                    .ifPresent(first.repoUrl()::set);
        }
        return first;
    }

    @Override
    public Predicate<? super Object> from() {
        return  c -> c instanceof CommitDiffAssertParentCtx;
    }

    @Override
    public Predicate<? super Object> to() {
        return c -> c instanceof MetaProgCtx;
    }
}
