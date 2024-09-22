package com.hayden.test_graph.commit_diff_context.ctx;

import com.hayden.test_graph.commit_diff_context.data_dep.CommitDiffDataDepBubbleNode;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.data_dep.ctx.DataDepMeta;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.stereotype.Component;

@Component
@ThreadScope
public record CommitDiffDataDepBubble() implements DataDepBubble {
    @Override
    public MetaCtx bubble() {
        return new DataDepMeta(this);
    }

    @Override
    public Class<? extends MetaCtx> bubbleClazz() {
        return DataDepMeta.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffDataDepBubbleNode;
    }

}
