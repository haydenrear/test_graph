package com.hayden.test_graph.commit_diff_context.assert_nodes.indexing;

import com.hayden.test_graph.assert_g.exec.single.AssertNode;
import com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.ctx.CommitDiffContextIndexingAssertCtx;
import com.hayden.test_graph.commit_diff_context.data_dep.indexing.ctx.CommitDiffContextIndexingDataDepBubble;
import com.hayden.test_graph.graph.edge.PreExecTestGraphEdge;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Graph edge that transfers indexing data dependency configuration to the assertion phase.
 * This ensures assertions have access to deployment information for validation.
 */
@Component
@ResettableThread
public class IndexingDataDepToAssertEdge implements CommitDiffContextIndexingAssertNode {

    CommitDiffContextIndexingDataDepBubble dataDepBubble;

    @Autowired
    @ResettableThread
    public void setDataDepBubble(CommitDiffContextIndexingDataDepBubble dataDepBubble) {
        this.dataDepBubble = dataDepBubble;
    }

    @Override
    public CommitDiffContextIndexingAssertCtx exec(CommitDiffContextIndexingAssertCtx c, MetaCtx h) {
        this.set(c, dataDepBubble);
        return c;
    }


    void set(CommitDiffContextIndexingAssertCtx assertCtx, CommitDiffContextIndexingDataDepBubble dataDepBubble) {
        // Transfer data dependency context to assertion context
        dataDepBubble.getIndexingCtx().optional()
                .ifPresent(assertCtx::setDataDepContext);
    }
}
