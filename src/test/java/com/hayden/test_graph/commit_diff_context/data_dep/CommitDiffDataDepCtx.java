package com.hayden.test_graph.commit_diff_context.data_dep;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.data_dep.ctx.DataDepCtx;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ResettableThread
public class CommitDiffDataDepCtx implements DataDepCtx {

    @Autowired
    @ResettableThread
    private CommitDiffDataDepBubble bubble;


    @Override
    public DataDepBubble bubble() {
        return bubble;
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
