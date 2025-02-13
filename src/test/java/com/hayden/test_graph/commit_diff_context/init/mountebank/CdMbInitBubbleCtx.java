package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.hayden.commitdiffmodel.codegen.types.*;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInitBubble;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpBubble;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.docker.ctx.DockerInitBubbleCtx;
import com.hayden.test_graph.init.mountebank.ctx.MbInitBubbleCtx;
import com.hayden.test_graph.init.mountebank.ctx.MbInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.sort.GraphSort;
import jakarta.annotation.PostConstruct;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
