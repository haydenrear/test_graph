package com.hayden.test_graph.commit_diff_context.init.repo_op.ctx;

import com.hayden.commitdiffmodel.codegen.types.*;
import com.hayden.commitdiffmodel.git.GitFactory;
import com.hayden.commitdiffmodel.git.RepositoryHolder;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.init.repo_op.RepoOpInitNode;
import com.hayden.test_graph.commit_diff_context.service.CallGraphQlQueryArgs;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.stream.StreamUtil;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

@Component
@ResettableThread
@RequiredArgsConstructor
public class RepoOpInit implements InitCtx {

    private Assertions assertions;

    @Autowired
    public void setAssertions(Assertions assertions) {
        this.assertions = assertions;
    }

    @Builder
    public record GraphQlQueries(File addRepo) {}

    public record CommitDiffData(@NotNull String sessionKey) {}

    public record RepoInitializations(List<RepoInitItem> initItems, ContextValue<Boolean> simultaneously) {

        public RepoInitializations(List<RepoInitItem> initItems, boolean simultaneously) {
            this(initItems, ContextValue.ofExisting(simultaneously));
        }

        public RepoInitializations(List<RepoInitItem> initItems) {
            this(initItems, ContextValue.empty());
        }

    }

    public void doOnPromptingOptions(Consumer<PromptingOptions> promptingOptions) {
        promptingOptions.accept(this.commitDiffContextValue.nextCommitRequest.getGitRepoRequestOptions().getPromptingOptions());
    }

    public void doOnRagOptions(Consumer<RagOptions> updateRagOptions) {
        updateRagOptions.accept(this.commitDiffContextValue.nextCommitRequest.getRagOptions());
        updateRagOptions.accept(this.commitDiffContextValue.doGitOperationRequest.getRagOptions());
    }

    @Builder
    public record RepositoryData(String url,
                                 String branchName,
                                 Path clonedUri) {

        public RepositoryData(String url, String branchName) {
            this(url, branchName, null);
        }

        public GitRepo toGitRepo() {
            return GitRepo.newBuilder()
                    .path(clonedUri.toAbsolutePath().toString()).build();
        }

        public GitBranch toGitBranch() {
            return GitBranch.newBuilder().branch(branchName).build();
        }

        public RepositoryData withBranch(String branchName) {
            return new RepositoryData(url, branchName, clonedUri);
        }

        public RepositoryData unzipped(Path unzippedTo) {
            var uz = StreamUtil.toStream(unzippedTo.toFile().listFiles()).findAny().map(File::toPath);
            return new RepositoryData(
                    uz.map(Path::toAbsolutePath).map(Path::toString).orElse(unzippedTo.toAbsolutePath().toString()),
                    branchName,
                    uz.orElse(unzippedTo));
        }

        public RepositoryData withClonedUri(Path clonedPath) {
            return new RepositoryData(url, branchName, clonedPath);
        }

        public RepositoryHolder.RepositoryArgs toRepositoryArgs() {
            return RepositoryHolder.RepositoryArgs.builder()
                    .branch(branchName)
                    .gitRepoDirectory(GitFactory.GitRepoDirectory.builder()
                            .repoDir(new File(url))
                            .sourceOfTruthDir(url)
                            .originalUri(url)
                            .build())
                    .build();
        }
    }

    @Builder
    public record CommitDiffContextGraphQlModel(GitRepoPromptingRequest nextCommitRequest,
                                                GitRepositoryRequest doGitOperationRequest,
                                                SessionKey sessionKey,
                                                ContextValue<Integer> numSecondsAsync) {

        public CommitDiffContextGraphQlModel(GitRepoPromptingRequest nextCommitRequest,
                                             GitRepositoryRequest doGitOperationRequest,
                                             SessionKey sessionKey) {
            this(nextCommitRequest, doGitOperationRequest, sessionKey, ContextValue.empty());
        }

        public void addRepoToContext(GitRepoQueryRequest gitRepositoryRequest) {
            this.nextCommitRequest.getGitRepoRequestOptions().getPromptingOptions().getIncludeRepoClosestCommits().add(gitRepositoryRequest);
        }

        public List<PrevDiff> prevDiffs() {
            return Optional.ofNullable(this.nextCommitRequest())
                    .flatMap(gpr -> Optional.ofNullable(gpr.getPrev()))
                    .map(PrevCommit::getDiffs)
                    .orElse(new ArrayList<>());
        }

        public Optional<String> commitMessage() {
            return Optional.ofNullable(nextCommitRequest)
                    .flatMap(gr -> Optional.ofNullable(gr.getCommitMessage()))
                    .flatMap(cm -> Optional.ofNullable(cm.getValue()));
        }

        public List<ContextData> getContextData() {
            return Optional.ofNullable(nextCommitRequest)
                    .flatMap(gr -> Optional.ofNullable(gr.getContextData()))
                    .orElse(new ArrayList<>());
        }

        public List<PrevRequests> getPrevRequests() {
            return Optional.ofNullable(nextCommitRequest)
                    .flatMap(gr -> Optional.ofNullable(gr.getPrevRequests()))
                    .orElse(new ArrayList<>());
        }

        public List<PromptDiff> stagedDiffs() {
            return Optional.ofNullable(nextCommitRequest())
                    .flatMap(g -> Optional.ofNullable(g.getStaged()))
                    .map(Staged::getDiffs)
                    .orElse(new ArrayList<>());
        }

        public void setMaxTimeBlameTree(int millis) {
            this.nextCommitRequest.getRagOptions().getBlameTreeOptions().setMaxTimeBlameTree(millis);
            this.doGitOperationRequest.getRagOptions().getBlameTreeOptions().setMaxTimeBlameTree(millis);
        }

        public void setMaxCommitDiffsBlameTree(int maxCommitDiffs) {
            this.nextCommitRequest.getRagOptions().getBlameTreeOptions().setMaxCommitDiffs(maxCommitDiffs);
            this.doGitOperationRequest.getRagOptions().getBlameTreeOptions().setMaxCommitDiffs(maxCommitDiffs);
        }

    }

    @Builder
    public record UserCodeData(String commitMessage) { }

    @Builder
    public record BubbleData(Path clonedTo) { }

    private final ContextValue<RepoOpInit.RepositoryData> repositoryData;
    private final ContextValue<UserCodeData> userCodeData;
    private final ContextValue<BubbleData> bubbleDataContextValue;
    private final ContextValue<RepoOpBubble> bubbleUnderlying;
    private final ContextValue<GraphQlQueries> queries;

    @Getter
    private CommitDiffContextGraphQlModel commitDiffContextValue;

    @Getter
    private final ContextValue<CommitDiffData> commitDiffData;

    @Getter
    private final RepoInitializations repoInitializations;

    public RepoOpInit() {
        this(ContextValue.empty(), ContextValue.empty(), ContextValue.empty(), ContextValue.empty(),
                ContextValue.empty(), ContextValue.empty(), new RepoInitializations(new ArrayList<>()));
    }

    public RagOptions doGitRagOptions() {
        return commitDiffContextValue.doGitOperationRequest.getRagOptions();
    }


    public void initializeCommitDiffContextValue() {
        if (this.commitDiffContextValue == null)
            this.commitDiffContextValue = CommitDiffContextGraphQlModel.builder()
                    .sessionKey(SessionKey.newBuilder().build())
                    .numSecondsAsync(ContextValue.empty())
                    .doGitOperationRequest(GitRepositoryRequest.newBuilder()
                            .sessionKey(new SessionKey())
                            .ragOptions(RagOptions.newBuilder()
                                    .parseGitOptions(ParseGitOptions.newBuilder().build())
                                    .blameTreeOptions(BlameTreeOptions.newBuilder().build())
                                    .build())
                            .gitRepoRequestOptions(GitRepoRequestOptions.newBuilder()
                                    .promptingOptions(PromptingOptions.newBuilder()
                                            .includeRepoClosestCommits(new ArrayList<>())
                                            .build())
                                    .build())
                            .build())
                    .nextCommitRequest(GitRepoPromptingRequest.newBuilder()
                            .sessionKey(new SessionKey())
                            .ragOptions(RagOptions.newBuilder()
                                    .parseGitOptions(ParseGitOptions.newBuilder().build())
                                    .blameTreeOptions(BlameTreeOptions.newBuilder().build())
                                    .build())
                            .gitRepoRequestOptions(GitRepoRequestOptions.newBuilder()
                                    .promptingOptions(PromptingOptions.newBuilder()
                                            .includeRepoClosestCommits(new ArrayList<>())
                                            .build())
                                    .build())
                            .build())
                    .build();
    }


    @Autowired
    public void setBubble(RepoOpBubble bubble) {
        this.bubbleUnderlying.swap(bubble);
        this.bubbleUnderlying.res().one().get().getRepoInit().swap(this);
        initializeCommitDiffContextValue();
    }

    public void setRepoData(RepositoryData repositoryData) {
        this.repositoryData.swap(repositoryData);
        this.commitDiffContextValue.nextCommitRequest.setGitRepo(repositoryData.toGitRepo());
        this.commitDiffContextValue.nextCommitRequest.setBranchName(repositoryData.branchName);
        this.commitDiffContextValue.doGitOperationRequest.setGitRepo(repositoryData.toGitRepo());
        this.commitDiffContextValue.doGitOperationRequest.setGitBranch(repositoryData.toGitBranch());
    }

    public void setSessionKey(SessionKey sessionKey) {
        this.commitDiffData.swap(new CommitDiffData(sessionKey.getKey()));
        addSessionKeyToRequests();
    }

    private void addSessionKeyToRequests() {
        var sessionKey = this.retrieveSessionKey();
        this.commitDiffContextValue.sessionKey.setKey(sessionKey);
        this.commitDiffContextValue.doGitOperationRequest.getSessionKey().setKey(sessionKey);
        this.commitDiffContextValue.nextCommitRequest.getSessionKey().setKey(sessionKey);
    }

    public ContextValue<RepositoryData> repoData() {
        return repositoryData;
    }

    public String getNextCommitMessageExpected() {
        return this.userCodeData.optional().map(UserCodeData::commitMessage).orElseGet(() -> {
            String error = "Could not find user commit message";
            assertions.assertSoftly(false, error);
            return error;
        });
    }

    public String retrieveSessionKey() {
        if(this.commitDiffData.isPresent()) {
            var cdd = this.commitDiffData.res().one()
                    .orElseGet(() -> {
                        var newKey = UUID.randomUUID().toString();
                        return new CommitDiffData(newKey);
                    });

            this.commitDiffData.swap(cdd);
        } else {
            var newKey = UUID.randomUUID().toString();
            this.commitDiffData.swap(new CommitDiffData(newKey));
        }

        return this.commitDiffData.res().get().sessionKey;
    }

    public ContextValue<GraphQlQueries> graphQlQueries() {
        return queries;
    }

    public void setCommitMessage(CommitMessage commitMessage) {
        this.commitDiffContextValue.nextCommitRequest()
                .setCommitMessage(commitMessage);
        this.userCodeData.swap(new UserCodeData(commitMessage.getValue()));
    }

    public CallGraphQlQueryArgs.CodeContextQueryArgs toCodeContextRequestArgs() {
        initializeCommitDiffContextValue();
        RepositoryData repoArgs = repoDataOrThrow();
        addSessionKeyToRequests();
        return CallGraphQlQueryArgs.CodeContextQueryArgs.builder()
                .commitDiffContextValue(this.commitDiffContextValue)
                .commitMessage(
                        userCodeData.optional()
                                .or(() -> Optional.of(UserCodeData.builder()
                                        .build()))
                                .map(UserCodeData::commitMessage)
                                .orElse(null))
                .gitRepoPath(repoArgs.url)
                .branchName(repoArgs.branchName)
                .build();
    }

    public CallGraphQlQueryArgs.CommitRequestArgs toCommitRequestArgs() {
        initializeCommitDiffContextValue();
        RepositoryData repoArgs = repoDataOrThrow();
        addSessionKeyToRequests();
        return CallGraphQlQueryArgs.CommitRequestArgs.builder()
                .commitDiffContextValue(this.commitDiffContextValue)
                .commitMessage(
                        userCodeData.optional()
                                .or(() -> Optional.of(UserCodeData.builder()
                                        .build()))
                                .map(UserCodeData::commitMessage)
                                .orElse(null))
                .gitRepoPath(repoArgs.url)
                .branchName(repoArgs.branchName)
                .build();
    }

    public UserCodeData userCodeDataOrThrow() {
        return userCodeData.res().orElseThrow();
    }

    public RepositoryData repoDataOrThrow() {
        return this.repositoryData.res().orElseThrow();
    }

    public RepositoryData repoDataOrNull() {
        return this.repositoryData
                .optional()
                .orElse(null);
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
