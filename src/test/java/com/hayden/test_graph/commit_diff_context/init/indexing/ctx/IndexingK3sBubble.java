package com.hayden.test_graph.commit_diff_context.init.indexing.ctx;

import com.hayden.test_graph.commit_diff_context.assert_nodes.codegen.CodegenBubble;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.commit_diff_context.init.indexing.IndexingK3sInitBubbleNode;
import com.hayden.test_graph.init.k3s.ctx.K3sInitBubbleCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ResettableThread
@RequiredArgsConstructor
public class IndexingK3sBubble implements InitBubble {

    @Getter
    private IndexingK3sInit k3sInit;

    @Autowired
    @ResettableThread
    public void setK3sInit(IndexingK3sInit k3sInit) {
        this.k3sInit = k3sInit;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof IndexingK3sInitBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext<InitBubble>>> dependsOn() {
        return List.of(K3sInitBubbleCtx.class);
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(IndexingK3sInit.class);
    }

}
