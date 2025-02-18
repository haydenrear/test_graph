package com.hayden.test_graph.commit_diff_context.init.mountebank.ctx;

import com.hayden.test_graph.commit_diff_context.init.mountebank.CdMbInitBubbleNode;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.docker.ctx.DockerInitBubbleCtx;
import com.hayden.test_graph.init.mountebank.ctx.MbInitBubbleCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ResettableThread
public class CdMbInitBubbleCtx implements MbInitBubbleCtx {

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CdMbInitBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(CdMbInitCtx.class);
    }

    @Override
    public List<Class<? extends InitBubble>> dependsOn() {
        return List.of(DockerInitBubbleCtx.class);
    }
}
