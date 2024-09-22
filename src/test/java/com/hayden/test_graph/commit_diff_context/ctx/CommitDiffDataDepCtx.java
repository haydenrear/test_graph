package com.hayden.test_graph.commit_diff_context.ctx;

import com.hayden.test_graph.commit_diff_context.data_dep.CommitDiffDataDepNode;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.data_dep.ctx.DataDepCtx;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.stereotype.Component;

@Component
@ThreadScope
public record CommitDiffDataDepCtx() implements DataDepCtx {
    @Override
    public DataDepBubble bubble() {
        return new CommitDiffDataDepBubble();
    }

    @Override
    public Class<? extends DataDepBubble> bubbleClazz() {
        return CommitDiffDataDepBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffDataDepNode;
    }

}
