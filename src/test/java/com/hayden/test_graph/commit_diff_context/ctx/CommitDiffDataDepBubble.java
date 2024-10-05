package com.hayden.test_graph.commit_diff_context.ctx;

import com.hayden.test_graph.commit_diff_context.data_dep.CommitDiffDataDepBubbleNode;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.data_dep.ctx.DataDepMeta;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ResettableThread
@RequiredArgsConstructor
public class CommitDiffDataDepBubble implements DataDepBubble {

    private DataDepMeta dataDepMeta;


    @Autowired
    public void setDataDepMeta(DataDepMeta meta) {
        this.dataDepMeta = meta;
        this.dataDepMeta.setBubbled(this);
    }

    // TODO: inject the hypergraph edges into these classes, and then input as an arg here the
    //      MetaProgCtx, and bubble here.
    @Override
    public MetaCtx bubble() {
        return this.dataDepMeta;
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
