package com.hayden.test_graph.commit_diff_context.service;

import com.hayden.commitdiffmodel.codegen.types.*;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.init.mountebank.CdMbInitBubbleCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.result.Result;
import com.hayden.utilitymodule.result.error.SingleError;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.GraphQlTransportException;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hayden.commitdiffmodel.graphql.GraphQlTemplates.*;

@Service
public class CommitDiff {

    @Autowired
    HttpSyncGraphQlClient graphQlClient;
    @Autowired
    @ResettableThread
    Assertions assertions;

    public interface CallGraphQlQueryArgs<T> {
        Class<T> clazz();
    }

    @Builder
    public record ValidateBranchAdded(String branchName, String gitRepoPath) implements CallGraphQlQueryArgs<GitRepoResult> {
        @Override
        public Class<GitRepoResult> clazz() {
            return GitRepoResult.class;
        }
    }

    @Builder
    public record AddCodeBranchArgs(String branchName, String gitRepoPath, String sessionKey) implements CallGraphQlQueryArgs<GitRepoResult> {
        @Override
        public Class<GitRepoResult> clazz() {
            return GitRepoResult.class;
        }

        public String sessionKey() {
            return Optional.ofNullable(sessionKey)
                    .orElse(UUID.randomUUID().toString());
        }
    }

    @Builder
    public record CommitRequestArgs(String branchName, String gitRepoPath, String commitMessage,
                                    CdMbInitBubbleCtx.CommitDiffContextGraphQlModel commitDiffContextValue) implements CallGraphQlQueryArgs<NextCommit> {
        @Override
        public Class<NextCommit> clazz() {
            return NextCommit.class;
        }
    }

    @Builder
    public record CommitDiffContextGraphQlError(List<ResponseError> errors, String error) implements SingleError {

        public CommitDiffContextGraphQlError(List<ResponseError> errors) {
            this(errors, null);
        }

        public CommitDiffContextGraphQlError(String errors) {
            this(new ArrayList<>(), errors);
        }

        @Override
        public String getMessage() {
            return errors.stream()
                    .map(re -> "%s: %s".formatted(re.getErrorType().toString(), re.getMessage()))
                    .collect(Collectors.joining(", "));
        }
    }

    private static @NotNull CommitDiffContextGraphQlError getGraphQlError(ClientGraphQlResponse clientGraphQlResponse) {
        return new CommitDiffContextGraphQlError(clientGraphQlResponse.getErrors());
    }

    public Result<GitRepoResult, CommitDiffContextGraphQlError> addCodeBranch(AddCodeBranchArgs commitRequestArgs) {
        return callGraphQlQuery(commitRequestArgs);
    }

    public Result<NextCommit, CommitDiffContextGraphQlError> requestCommit(CommitRequestArgs commitRequestArgs) {
        return callGraphQlQuery(commitRequestArgs);
    }

    public Result<GitRepoResult, CommitDiffContextGraphQlError> validateCodeBranch(ValidateBranchAdded commitRequestArgs) {
        return callGraphQlQuery(commitRequestArgs);
    }

    public <T> Result<T, CommitDiffContextGraphQlError> callGraphQlQuery(CallGraphQlQueryArgs<T> graphQlQueryArgs) {
        return switch (graphQlQueryArgs) {
            case ValidateBranchAdded branchAdded ->
                    this.doWithGraphQl(client -> {
                        var gqlResult = client.document(RETRIEVE_CODE_BRANCH()
                                .formatted(branchAdded.branchName, branchAdded.gitRepoPath));
                        return toRes(gqlResult.executeSync(), graphQlQueryArgs);
                    });
            case AddCodeBranchArgs branchAdded ->
                    this.doWithGraphQl(client -> {
                        var gqlResult = client.document(ADD_CODE_BRANCH()
                                .formatted("ADD_BRANCH", branchAdded.branchName, branchAdded.gitRepoPath, branchAdded.sessionKey()));
                        return toRes(gqlResult.executeSync(), graphQlQueryArgs);
                    });
            case CommitRequestArgs commitRequestArgs ->
                    this.doWithGraphQl(client -> {
                        var gqlResult = client.document(NEXT_COMMIT_TEMPLATE()
                                .formatted(commitRequestArgs.branchName,
                                        commitRequestArgs.gitRepoPath,
                                        commitRequestArgs.commitMessage,
                                        retrieveSessionKey(commitRequestArgs),
                                        commitRequestArgs.commitDiffContextValue.stagedDiffs()
                                                // TODO: to graphql
                                                .stream().map(pd -> """
                                                        """).collect(Collectors.joining(", ")),
                                        commitRequestArgs.commitDiffContextValue.prevDiffs().stream()
                                                .map(pd -> "")
                                                .collect(Collectors.joining(", ")),
                                        commitRequestArgs.commitDiffContextValue.commitMessage()
                                                .orElse(""),
                                        commitRequestArgs.commitDiffContextValue.sessionKey()
                                                .orElse(""),
                                        commitRequestArgs.commitDiffContextValue.getContextData()
                                                .stream().map(pd -> "")
                                                .collect(Collectors.joining(", ")),
                                        commitRequestArgs.commitDiffContextValue
                                                .getPrevRequests().stream().map(pd -> "")
                                                .collect(Collectors.joining(", "))));
                        return toRes(gqlResult.executeSync(), graphQlQueryArgs);
                    });
            default ->
                    throw new IllegalStateException("Unexpected value: " + graphQlQueryArgs);
        };
    }

    private static String retrieveSessionKey(CommitRequestArgs commitRequestArgs) {
        return repoItem(commitRequestArgs.commitDiffContextValue.addRepo())
                .or(() -> repoItem(commitRequestArgs.commitDiffContextValue.repositoryRequest()))
                .orElse("");
    }

    private static @NotNull Optional<String> repoItem(GitRepoPromptingRequest value) {
        return Optional.ofNullable(value)
                .flatMap(g -> Optional.ofNullable(g.getSessionKey()))
                .flatMap(sess -> Optional.ofNullable(sess.getKey()));
    }

    private static @NotNull Optional<String> repoItem(GitRepositoryRequest value) {
        return Optional.ofNullable(value)
                .flatMap(g -> Optional.ofNullable(g.getSessionKey()))
                .flatMap(sess -> Optional.ofNullable(sess.getKey()));
    }

    public <T> Result<T, CommitDiffContextGraphQlError> doWithGraphQl(Function<HttpSyncGraphQlClient, Result<T, CommitDiffContextGraphQlError>> toDo) {
        try {
            return toDo.apply(this.graphQlClient);
        } catch (GraphQlTransportException |
                 ResourceAccessException ce) {
//            assertions.assertThat(false)
//                    .withFailMessage("Could not connect to graphQL: %s".formatted(ce.getMessage()))
//                    .isTrue();
            return Result.err(new CommitDiffContextGraphQlError(ce.getMessage()));
        }
    }

    private <T> @NotNull Result<T, CommitDiffContextGraphQlError> toRes(ClientGraphQlResponse gqlResult,
                                                                        CallGraphQlQueryArgs<T> args) {
        return Result.from(gqlResult.toEntity(args.clazz()), getGraphQlError(gqlResult));
    }

}
