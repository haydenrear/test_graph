package com.hayden.test_graph.commit_diff_context.service;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.ctx.CommitDiffInit;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.result.Result;
import com.hayden.utilitymodule.result.error.ErrorCollect;
import lombok.Builder;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.GraphQlTransportException;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommitDiff {

    @Autowired
    @ResettableThread
    CommitDiffInit commitDiffInit;

    @Autowired
    HttpSyncGraphQlClient graphQlClient;
    @Autowired
    @ResettableThread
    Assertions assertions;

    private static final @Language("graphql") String NEXT_COMMIT_GRAPH_QL_TEMPLATE = """
                    mutation {
                        doRequest(
                            gitRepoPromptingRequest: {
                                    branchName: "%s",
                                    gitRepo: {path: "%s"} ,
                                    commitMessage: {value: "%s"},
                                    ragOptions: {commitsPerK: 3, topK: 3, maxDepth: 3}
                                  }
                            ) {
                            diffs {
                                diffType
                            },
                            commitMessage
                        }
                }
    """;

    private static final @Language("graphql") String ADD_CODE_BRANCH_TEMPLATE = """
                    mutation {
                        doGit(
                            gitOperation: {
                                    operation: ADD_BRANCH,
                                    gitBranch: {branch: "%s"} ,
                                    gitRepo: {path: "%s"}
                                  }
                            ) {
                            branch,
                            url,
                            repoStatus,
                            error {
                                message
                            }
                        }
                }
    """;

    @Builder
    public record CommitRequestArgs(String branchName, String gitRepoPath, String commitMessage) {}

    @Builder
    public record AddCodeBranchArgs(String branchName, String gitRepoPath, String commitMessage) {}

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

    public Result<Void, CommitDiffContextGraphQlError> requestCommit(CommitRequestArgs commitRequestArgs) {
        return doWithGraphQl(graphQlClient -> {
            String document = NEXT_COMMIT_GRAPH_QL_TEMPLATE.formatted(commitRequestArgs.branchName, commitRequestArgs.gitRepoPath, commitRequestArgs.commitMessage);
            ClientGraphQlResponse clientGraphQlResponse = graphQlClient.document(document)
                    .executeSync();
            var found = clientGraphQlResponse
                    .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

            return Result.from(
                    null,
                    getGraphQlError(clientGraphQlResponse)
            );
        });
    }

    private static @NotNull CommitDiffContextGraphQlError getGraphQlError(ClientGraphQlResponse clientGraphQlResponse) {
        return new CommitDiffContextGraphQlError(clientGraphQlResponse.getErrors());
    }

    public Result<Void, CommitDiffContextGraphQlError> addCodeBranch(AddCodeBranchArgs commitRequestArgs) {
        return doWithGraphQl(graphQlClient -> {
            String document = ADD_CODE_BRANCH_TEMPLATE.formatted(commitRequestArgs.branchName, commitRequestArgs.gitRepoPath);
            ClientGraphQlResponse clientGraphQlResponse = graphQlClient.document(document)
                    .executeSync();
            var found = clientGraphQlResponse
                    .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

            return Result.from(
                    null,
                    getGraphQlError(clientGraphQlResponse)
            );
        });
    }

    public <T> Result<T, CommitDiffContextGraphQlError> doWithGraphQl(Function<HttpSyncGraphQlClient, Result<T, CommitDiffContextGraphQlError>> toDo) {
        try {
            return toDo.apply(this.graphQlClient);
        } catch (GraphQlTransportException | ResourceAccessException ce) {
            assertions.assertThat(false)
                    .withFailMessage("Could not connect to graphQL: %s".formatted(ce.getMessage()))
                    .isTrue();
            return Result.err(new CommitDiffContextGraphQlError(ce.getMessage()));
        }
    }

}
