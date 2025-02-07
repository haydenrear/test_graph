package com.hayden.test_graph.commit_diff_context.assert_nodes.parent;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ResettableThread
public class CommitDiffAssertParentNode implements CommitDiffCtxParentAssertNode {

    @Autowired
    private Assertions assertions;
    @Autowired
    private CommitDiff commitDiff;

    @Override
    public Class<? extends CommitDiffAssertParentCtx> clzz() {
        return CommitDiffAssertParentCtx.class;
    }

    @Override
    @Idempotent(returnArg = 0)
    public CommitDiffAssertParentCtx exec(CommitDiffAssertParentCtx c, MetaCtx h) {
        c.getValidated().swap(true);
        return c;
    }
}
