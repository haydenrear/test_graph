package com.hayden.test_graph.commit_diff_context.assert_nodes.codegen;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.assert_g.ctx.AssertMeta;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssertBubble;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssertBubbleNode;
import com.hayden.test_graph.commit_diff_context.assert_nodes.parent.CommitDiffCtxParentBubble;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
@ResettableThread
@RequiredArgsConstructor
public class CodegenBubble implements CommitDiffAssertBubble {


    private AssertMeta assertMeta;

    @Autowired
    public void setAssertMeta(AssertMeta assertMeta) {
        this.assertMeta = assertMeta;
        this.assertMeta.setBubbled(this);
    }

    @Override
    public MetaCtx bubble() {
        return this.assertMeta;
    }

    @Override
    public Class<? extends MetaCtx> bubbleClazz() {
        return AssertMeta.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CodegenAssertBubbleNode;
    }

    @Override
    public Optional<Class<? extends TestGraphContext>> parentTy() {
        return Optional.of(CommitDiffCtxParentBubble.class);
    }

}
