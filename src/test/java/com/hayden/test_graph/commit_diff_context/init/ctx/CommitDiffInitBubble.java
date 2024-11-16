package com.hayden.test_graph.commit_diff_context.init.ctx;

import com.hayden.test_graph.commit_diff_context.init.CommitDiffInitBubbleNode;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitMeta;
import com.hayden.test_graph.init.docker.ctx.DockerInitBubbleCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@ResettableThread
@RequiredArgsConstructor
public final class CommitDiffInitBubble implements InitBubble {

    private final ContextValue<CommitDiffInit.RepositoryData> repositoryData;

    private InitMeta initMeta;


    public CommitDiffInitBubble() {
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
        return n instanceof CommitDiffInitBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext<MetaCtx>>> dependsOn() {
        return List.of(DockerInitBubbleCtx.class);
    }

    public ContextValue<CommitDiffInit.RepositoryData> repositoryData() {
        return repositoryData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        var that = (CommitDiffInitBubble) obj;
        return Objects.equals(this.repositoryData, that.repositoryData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryData);
    }

    @Override
    public String toString() {
        return "CommitDiffInitBubble[" +
                "repositoryData=" + repositoryData + ']';
    }

}
