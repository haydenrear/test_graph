package com.hayden.test_graph.commit_diff_context.service;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.codegen.types.GitRepoResult;
import com.hayden.test_graph.codegen.types.NextCommit;
import com.hayden.test_graph.commit_diff_context.init.ctx.CommitDiffInit;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.result.Result;
import com.hayden.utilitymodule.result.error.ErrorCollect;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hayden.test_graph.commit_diff_context.service.GraphQlTemplates.*;

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
    public record AddCodeBranchArgs(String branchName, String gitRepoPath) implements CallGraphQlQueryArgs<GitRepoResult> {
        @Override
        public Class<GitRepoResult> clazz() {
            return GitRepoResult.class;
        }
    }

    @Builder
    public record CommitRequestArgs(String branchName, String gitRepoPath, String commitMessage) implements CallGraphQlQueryArgs<NextCommit> {
        @Override
        public Class<NextCommit> clazz() {
            return NextCommit.class;
        }
    }

    @Builder
    public record CommitDiffContextGraphQlError(List<ResponseError> errors, String error) implements ErrorCollect {

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
                        var gqlResult = client.document(RETRIEVE_CODE_BRANCH_TEMPLATE
                                .formatted(branchAdded.branchName, branchAdded.gitRepoPath));
                        return toRes(gqlResult.executeSync(), graphQlQueryArgs);
                    });
            case AddCodeBranchArgs branchAdded ->
                    this.doWithGraphQl(client -> {
                        var gqlResult = client.document(ADD_CODE_BRANCH_TEMPLATE
                                .formatted(branchAdded.branchName, branchAdded.gitRepoPath));
                        return toRes(gqlResult.executeSync(), graphQlQueryArgs);
                    });
            case CommitRequestArgs commitRequestArgs ->
                    this.doWithGraphQl(client -> {
                        var gqlResult = client.document(NEXT_COMMIT_GRAPH_QL_TEMPLATE
                                .formatted(commitRequestArgs.branchName, commitRequestArgs.gitRepoPath, commitRequestArgs.commitMessage));
                        return toRes(gqlResult.executeSync(), graphQlQueryArgs);
                    });
            default ->
                    throw new IllegalStateException("Unexpected value: " + graphQlQueryArgs);
        };
    }

    public <T> Result<T, CommitDiffContextGraphQlError> doWithGraphQl(Function<HttpSyncGraphQlClient, Result<T, CommitDiffContextGraphQlError>> toDo) {
        try {
            return toDo.apply(this.graphQlClient);
        } catch (GraphQlTransportException |
                 ResourceAccessException ce) {
            assertions.assertThat(false)
                    .withFailMessage("Could not connect to graphQL: %s".formatted(ce.getMessage()))
                    .isTrue();
            return Result.err(new CommitDiffContextGraphQlError(ce.getMessage()));
        }
    }

    private <T> @NotNull Result<T, CommitDiffContextGraphQlError> toRes(ClientGraphQlResponse gqlResult,
                                                                        CallGraphQlQueryArgs<T> args) {
        return Result.from(gqlResult.toEntity(args.clazz()), getGraphQlError(gqlResult));
    }

}
