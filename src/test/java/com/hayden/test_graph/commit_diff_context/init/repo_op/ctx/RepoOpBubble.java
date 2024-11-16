package com.hayden.test_graph.commit_diff_context.init.repo_op.ctx;

import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.CommitDiffInitBubbleNode;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.commit_diff_context.init.repo_op.RepoOpInitBubbleNode;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitMeta;
import com.hayden.test_graph.init.docker.ctx.DockerInitBubbleCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ResettableThread
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class RepoOpBubble implements InitBubble {

    private final ContextValue<RepoOpInit.RepositoryData> repositoryData;

    private InitMeta initMeta;


    public RepoOpBubble() {
        this(ContextValue.empty());
    }

    @Autowired
    public void setInitMeta(InitMeta initMeta) {
        this.initMeta = initMeta;
        this.initMeta.setBubbled(this);
    }

    @Override
    public InitMeta bubble() {
        return this.initMeta;
    }

    @Override
    public Class<? extends MetaCtx> bubbleClazz() {
        return InitMeta.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof RepoOpInitBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext<MetaCtx>>> dependsOn() {
        return List.of(DockerInitBubbleCtx.class);
    }

    public ContextValue<RepoOpInit.RepositoryData> repositoryData() {
        return repositoryData;
    }

}
