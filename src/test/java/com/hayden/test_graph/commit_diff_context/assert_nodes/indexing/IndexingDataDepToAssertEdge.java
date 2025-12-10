package com.hayden.test_graph.commit_diff_context.assert_nodes.indexing;

import com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.ctx.CommitDiffContextIndexingAssertCtx;
import com.hayden.test_graph.commit_diff_context.data_dep.indexing.ctx.CommitDiffContextIndexingDataDepCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Graph edge that transfers indexing data dependency configuration to the assertion phase.
 * This ensures assertions have access to deployment information for validation.
 */
@Component
@ResettableThread
@Profile("indexing")
public class IndexingDataDepToAssertEdge implements CommitDiffContextIndexingAssertNode {

    CommitDiffContextIndexingDataDepCtx dataDepBubble;

    @Autowired
    @ResettableThread
    public void setDataDepBubble(CommitDiffContextIndexingDataDepCtx dataDepBubble) {
        this.dataDepBubble = dataDepBubble;
    }

    @Override
    public CommitDiffContextIndexingAssertCtx exec(CommitDiffContextIndexingAssertCtx c, MetaCtx h) {
        this.set(c, dataDepBubble);
        return c;
    }

    void set(CommitDiffContextIndexingAssertCtx assertCtx, CommitDiffContextIndexingDataDepCtx dataDepBubble) {
        assertCtx.setDataDepContext(dataDepBubble);
    }
}
