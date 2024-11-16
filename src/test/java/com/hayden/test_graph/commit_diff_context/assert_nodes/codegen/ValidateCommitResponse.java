package com.hayden.test_graph.commit_diff_context.assert_nodes.codegen;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssert;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ResettableThread
public class ValidateCommitResponse implements CodegenAssertNode {

    @Override
    public Class<? extends Codegen> clzz() {
        return Codegen.class;
    }

    @Override
    @Idempotent(returnArg = 0)
    public Codegen exec(Codegen c, MetaCtx h) {
        c.getUserCode().res()
                .flatMap(ud -> c.repoUrl().res().map(r -> Map.entry(ud, r)))
                .ifPresent(ud -> {
                    // TODO assert... only if user code data is set
                });
        return c;
    }
}
