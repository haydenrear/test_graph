package com.hayden.test_graph.init.k3s.ctx;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.k3s.exec.K3sInitBubbleNode;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.stereotype.Component;

import java.util.List;

@ResettableThread
@Component
public class K3sInitBubbleCtx implements InitBubble {


    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof K3sInitBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(K3sInitCtx.class);
    }
}
