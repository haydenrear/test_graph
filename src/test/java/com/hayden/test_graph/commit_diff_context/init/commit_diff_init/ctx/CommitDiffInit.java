package com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx;

import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.CommitDiffInitNode;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;

@Component
@ResettableThread
@RequiredArgsConstructor
public final class CommitDiffInit implements InitCtx {

    private final ContextValue<UserCodeData> userCodeData;
    private final ContextValue<BubbleData> bubbleDataContextValue;
    private final ContextValue<CommitDiffInitBubble> bubbleUnderlying;
    private final ContextValue<GraphQlQueries> queries;

    @Builder
    public record GraphQlQueries(File addRepo) {}

    public CommitDiffInit() {
        this(ContextValue.empty(), ContextValue.empty(), ContextValue.empty(), ContextValue.empty());
    }

    @Autowired
    public void setBubble(CommitDiffInitBubble bubble) {
        this.bubbleUnderlying.set(bubble);
    }

    @Builder
    public record RepositoryData(String url,
                                 String branchName) {
    }

    @Builder
    public record UserCodeData(String commitMessage) { }

    @Builder
    public record BubbleData(Path clonedTo) { }

    public ContextValue<RepositoryData> repoData() {
        return bubbleUnderlying.res().get().repositoryData();
    }

    public ContextValue<GraphQlQueries> graphQlQueries() {
        return queries;
    }

    public CommitDiff.CommitRequestArgs toCommitRequestArgs() {
        RepositoryData repoArgs = repoDataOrThrow();
        return CommitDiff.CommitRequestArgs.builder()
                .commitMessage(userCodeDataOrThrow().commitMessage)
                .gitRepoPath(repoArgs.url)
                .branchName(repoArgs.branchName)
                .build();
    }

    public UserCodeData userCodeDataOrThrow() {
        return userCodeData.res().orElseThrow();
    }

    public RepositoryData repoDataOrThrow() {
        return this.bubbleUnderlying.res().get().repositoryData().res().orElseThrow();
    }


    @Override
    public CommitDiffInitBubble bubble() {
        return this.bubbleUnderlying.res().get();
    }

    @Override
    public Class<? extends InitBubble> bubbleClazz() {
        return CommitDiffInitBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffInitNode;
    }

    public ContextValue<UserCodeData> userCodeData() {
        return userCodeData;
    }

    public ContextValue<BubbleData> bubbleDataContextValue() {
        return bubbleDataContextValue;
    }

    public ContextValue<CommitDiffInitBubble> bubbleUnderlying() {
        return bubbleUnderlying;
    }

}
