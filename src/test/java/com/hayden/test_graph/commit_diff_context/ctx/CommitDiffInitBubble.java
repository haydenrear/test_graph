package com.hayden.test_graph.commit_diff_context.ctx;

import com.hayden.test_graph.commit_diff_context.init.CommitDiffInitBubbleNode;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitMeta;
import com.hayden.test_graph.init.docker.ctx.DockerInitBubbleCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ThreadScope
public record CommitDiffInitBubble() implements InitBubble {

    @Override
    public MetaCtx bubble() {
        return new InitMeta(this);
    }

    @Override
    public Class<? extends MetaCtx> bubbleClazz() {
        return InitMeta.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffInitBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext<MetaCtx>>> dependsOn() {
        return List.of(DockerInitBubbleCtx.class);
    }
}
