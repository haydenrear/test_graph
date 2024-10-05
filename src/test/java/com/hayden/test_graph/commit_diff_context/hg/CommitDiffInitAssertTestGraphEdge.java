package com.hayden.test_graph.commit_diff_context.hg;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.commit_diff_context.ctx.CommitDiffAssert;
import com.hayden.test_graph.commit_diff_context.ctx.CommitDiffInitBubble;
import com.hayden.test_graph.graph.edge.PreExecTestGraphEdge;
import com.hayden.test_graph.graph.edge.TestGraphEdge;
import com.hayden.test_graph.init.ctx.InitMeta;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.ctx.MetaProgCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
@ResettableThread
public class CommitDiffInitAssertTestGraphEdge implements PreExecTestGraphEdge<CommitDiffAssert, AssertBubble> {

    @Override
    public CommitDiffAssert edge(CommitDiffAssert first, MetaCtx s) {
        if (s instanceof MetaProgCtx second) {
            var i = second.retrieveBubbled(CommitDiffInitBubble.class)
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
        return  c -> c instanceof CommitDiffAssert;
    }

    @Override
    public Predicate<? super Object> to() {
        return c -> c instanceof MetaProgCtx;
    }
}
