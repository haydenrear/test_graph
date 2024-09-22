package com.hayden.test_graph.commit_diff_context.ctx;

import com.hayden.test_graph.commit_diff_context.init.CommitDiffInitNode;
import com.hayden.test_graph.commit_diff_context.service.CommitDiffContext;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.docker.ctx.DockerInitBubbleCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import com.hayden.utilitymodule.sort.GraphSort;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
@ThreadScope
public record CommitDiffInit(
        ContextValue<RepositoryData> repoData,
        ContextValue<UserCodeData> userCodeData,
        ContextValue<BubbleData> bubbleDataContextValue
) implements InitCtx {

    public CommitDiffInit() {
        this(ContextValue.empty(), ContextValue.empty(),
                ContextValue.empty());
    }

    @Builder
    public record RepositoryData(String url, String branchName) {}

    @Builder
    public record UserCodeData(String commitMessage) {}

    @Builder
    public record BubbleData(Path clonedTo) {}


    public CommitDiffContext.CommitRequestArgs toCommitRequestArgs() {
        RepositoryData repoArgs = repoDataOrThrow();
        return CommitDiffContext.CommitRequestArgs.builder()
                .commitMessage(userCodeDataOrThrow().commitMessage)
                .gitRepoPath(repoArgs.url)
                .branchName(repoArgs.branchName)
                .build();
    }

    public UserCodeData userCodeDataOrThrow() {
        return userCodeData.res().orElseThrow();
    }

    public RepositoryData repoDataOrThrow() {
        return repoData.res().orElseThrow();
    }


    @Override
    public CommitDiffInitBubble bubble() {
        return new CommitDiffInitBubble();
    }

    @Override
    public Class<? extends InitBubble> bubbleClazz() {
        return CommitDiffInitBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffInitNode;
    }


}
