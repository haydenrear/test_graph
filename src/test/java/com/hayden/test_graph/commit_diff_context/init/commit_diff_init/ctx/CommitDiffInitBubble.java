package com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx;

import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.CommitDiffInitBubbleNode;
import com.hayden.test_graph.commit_diff_context.init.mountebank.CdMbInitBubbleCtx;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpBubble;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.docker.ctx.DockerInitBubbleCtx;
import com.hayden.test_graph.init.mountebank.ctx.MbInitBubbleCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ResettableThread
@EqualsAndHashCode
@ToString
public class CommitDiffInitBubble implements InitBubble {

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffInitBubbleNode;
    }

    @Override
    public List<Class<? extends InitBubble>> dependsOn() {
        return List.of(DockerInitBubbleCtx.class);
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(CommitDiffInit.class);
    }
}
