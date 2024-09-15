package com.hayden.test_graph.commit_diff_context.service;

import com.hayden.test_graph.commit_diff_context.ctx.CommitDiffInit;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommitDiffContext {

    @Autowired
    @ThreadScope
    CommitDiffInit commitDiffInit;

    public void requestNextCommit() {

    }

}
