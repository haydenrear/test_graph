package com.hayden.test_graph.commit_diff_context.init.commit_diff_init;

import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.init.exec.single.InitNode;

public interface CommitDiffInitNode extends InitNode<CommitDiffInit> {

    default Class<? extends CommitDiffInit> clzz() {
        return CommitDiffInit.class;
    }
}
