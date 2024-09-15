package com.hayden.test_graph.commit_diff_context.init;

import com.hayden.test_graph.commit_diff_context.ctx.CommitDiffInit;
import com.hayden.test_graph.init.exec.single.InitNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InitializeRepo implements CommitDiffInitNode{

    @Override
    public CommitDiffInit exec(CommitDiffInit c, MetaCtx h) {
        var repoData = c.repoData().optional().get();
        // clone repo, add to context
        return c;
    }

    @Override
    public Class<? extends CommitDiffInit> clzz() {
        return CommitDiffInit.class;
    }

    @Override
    public List<Class> dependsOn() {
        return List.of();
    }
}
