package com.hayden.test_graph.commit_diff_context.assert_nodes.next_commit;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ResettableThread
public class ValidateNextCommitResponse implements NextCommitAssertNode {

    @Autowired
    private Assertions assertions;

    @Override
    @Idempotent(returnArg = 0)
    public NextCommitAssert exec(NextCommitAssert c, MetaCtx h) {
        var res = c.getNextCommitInfo().res();
        assertions.assertSoftly(res.isPresent(), "Expected a strongly next commit", "Found next commit");

        res.one().map(NextCommitAssert.NextCommitMetadata::nc)
                .ifPresent(ncm -> {

                });
        return c;
    }
}
