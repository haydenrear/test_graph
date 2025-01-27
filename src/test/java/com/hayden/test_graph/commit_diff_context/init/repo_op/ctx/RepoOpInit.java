package com.hayden.test_graph.commit_diff_context.init.repo_op.ctx;

import com.hayden.commitdiffmodel.codegen.types.GitRepoPromptingRequest;
import com.hayden.commitdiffmodel.codegen.types.GitRepositoryRequest;
import com.hayden.test_graph.commit_diff_context.init.mountebank.CdMbInitBubbleCtx;
import com.hayden.test_graph.commit_diff_context.init.repo_op.RepoOpInitNode;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

@Component
@ResettableThread
@RequiredArgsConstructor
public final class RepoOpInit implements InitCtx {

    private final ContextValue<UserCodeData> userCodeData;
    private final ContextValue<BubbleData> bubbleDataContextValue;
    private final ContextValue<RepoOpBubble> bubbleUnderlying;
    private final ContextValue<GraphQlQueries> queries;

    @Getter
    private final ContextValue<CommitDiffData> commitDiffData;

    @Getter
    private final RepoInitializations repoInitializations;

    public record CommitDiffData(@NotNull String sessionKey) {}

    public sealed interface RepoInitItem {

        Comparator<RepoInitItem> c = new Comparator<RepoInitItem>() {
            static List<Class<? extends RepoInitItem>> ordering = List.of(AddCodeBranch.class, AddEmbeddings.class);
            @Override
            public int compare(RepoInitItem o1, RepoInitItem o2) {
                return Integer.compare(ordering.indexOf(o1.getClass()), ordering.indexOf(o2.getClass()));
            }
        };

        record AddCodeBranch(RepositoryData repositoryData) implements RepoInitItem {}
        record AddEmbeddings() implements RepoInitItem {}
    }

    public record RepoInitializations(List<RepoInitItem> initItems) {}

    @Builder
    public record GraphQlQueries(File addRepo) {}

    public RepoOpInit() {
        this(ContextValue.empty(), ContextValue.empty(), ContextValue.empty(), ContextValue.empty(),
                ContextValue.empty(), new RepoInitializations(new ArrayList<>()));
    }

    @Autowired
    public void setBubble(RepoOpBubble bubble) {
        this.bubbleUnderlying.set(bubble);
    }

    @Builder
    public record RepositoryData(String url,
                                 String branchName) { }

    @Builder
    public record UserCodeData(String commitMessage) { }

    @Builder
    public record BubbleData(Path clonedTo) { }

    public ContextValue<RepositoryData> repoData() {
        return bubbleUnderlying.res().one().get().repositoryData();
    }

    public String retrieveSessionKey() {
        if(this.commitDiffData.isPresent()) {
            var cdd = this.commitDiffData.res().one()
                    .orElseGet(() -> {
                        var newKey = UUID.randomUUID().toString();
                        return new CommitDiffData(newKey);
                    });

            this.commitDiffData.set(cdd);
        } else {
            var newKey = UUID.randomUUID().toString();
            this.commitDiffData.set(new CommitDiffData(newKey));
        }

        return this.commitDiffData.res().get().sessionKey;
    }

    public ContextValue<GraphQlQueries> graphQlQueries() {
        return queries;
    }

    public CommitDiff.CommitRequestArgs toCommitRequestArgs(CdMbInitBubbleCtx bubbleCtx) {
        RepositoryData repoArgs = repoDataOrThrow();

        return CommitDiff.CommitRequestArgs.builder()
                .commitDiffContextValue(bubbleCtx.getCommitDiffContextValue())
                .commitMessage(userCodeDataOrThrow().commitMessage)
                .gitRepoPath(repoArgs.url)
                .branchName(repoArgs.branchName)
                .build();
    }

    public UserCodeData userCodeDataOrThrow() {
        return userCodeData.res().orElseThrow();
    }

    public RepositoryData repoDataOrThrow() {
        return this.bubbleUnderlying.res().one().get().repositoryData().res().orElseThrow();
    }


    @Override
    public RepoOpBubble bubble() {
        return this.bubbleUnderlying.res().one().get();
    }

    @Override
    public Class<RepoOpBubble> bubbleClazz() {
        return RepoOpBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof RepoOpInitNode;
    }

    public ContextValue<UserCodeData> userCodeData() {
        return userCodeData;
    }

    public ContextValue<BubbleData> bubbleDataContextValue() {
        return bubbleDataContextValue;
    }

    public ContextValue<RepoOpBubble> bubbleUnderlying() {
        return bubbleUnderlying;
    }

}
