package com.hayden.test_graph.commit_diff_context.assert_nodes;

import com.hayden.test_graph.commit_diff_context.ctx.CommitDiffAssert;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import org.springframework.stereotype.Component;

@Component
public class ValidateCommitResponse implements CommitDiffAssertNode{

    @Override
    public Class<? extends CommitDiffAssert> clzz() {
        return CommitDiffAssert.class;
    }

    @Override
    public CommitDiffAssert exec(CommitDiffAssert c, MetaCtx h) {
        return c;
    }
}
