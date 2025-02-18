package com.hayden.test_graph.commit_diff_context.init.repo_op;

import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.init.exec.single.InitNode;

public interface RepoOpInitNode extends InitNode<RepoOpInit> {

    default Class<? extends RepoOpInit> clzz() {
        return RepoOpInit.class;
    }
}
