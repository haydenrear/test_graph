package com.hayden.test_graph.commit_diff_context.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hayden.commitdiffmodel.codegen.client.BranchGraphQLQuery;
import com.hayden.commitdiffmodel.codegen.client.DoCommitGraphQLQuery;
import com.hayden.commitdiffmodel.codegen.client.DoGitGraphQLQuery;
import com.hayden.commitdiffmodel.codegen.types.*;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.init.mountebank.CdMbInitBubbleCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.result.Result;
import com.hayden.utilitymodule.result.error.SingleError;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hayden.commitdiffmodel.graphql.GraphQlTemplates.*;

@Slf4j
@Service
public class CommitDiff {

    @Autowired
    DgsGraphQlClient graphQlClient;
    @Autowired
    @ResettableThread
    Assertions assertions;
    @Autowired
    private ObjectMapper objectMapper;

    public interface CallGraphQlQueryArgs<T> {
        Class<T> clazz();

        String key();
    }

    @Builder
    public record ValidateBranchAdded(String branchName, String gitRepoPath) implements CallGraphQlQueryArgs<GitRepoResult> {
        @Override
        public Class<GitRepoResult> clazz() {
            return GitRepoResult.class;
        }

        @Override
        public String key() {
            return "branch";
        }
    }

    @Builder
    public record AddCodeBranchArgs(String branchName, String gitRepoPath, String sessionKey) implements CallGraphQlQueryArgs<GitRepoResult> {
        @Override
        public Class<GitRepoResult> clazz() {
            return GitRepoResult.class;
        }

        @Override
        public String key() {
            return "doGit";
        }

        public String sessionKey() {
            return Optional.ofNullable(sessionKey)
                    .orElse(UUID.randomUUID().toString());
        }
    }

    @Builder
    public record AddEmbeddingsArgs(String branchName, String gitRepoPath, String sessionKey)
            implements CallGraphQlQueryArgs<GitRepoResult> {
        @Override
        public Class<GitRepoResult> clazz() {
            return GitRepoResult.class;
        }

        @Override
        public String key() {
            return "doGit";
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

        @Override
        public String key() {
            return "doCommit";
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

    public Result<GitRepoResult, CommitDiffContextGraphQlError> addEmbeddings(AddEmbeddingsArgs commitRequestArgs) {
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
            case ValidateBranchAdded(String branchName, String gitRepoPath) ->
                    this.doWithGraphQl(client -> {
                        var doGitGraphQLQuery = BranchGraphQLQuery.newRequest()
                                .gitRepo(GitRepoQueryRequest.newBuilder()
                                        .gitBranch(GitBranch.newBuilder().branch(branchName).build())
                                        .gitRepo(GitRepo.newBuilder().path(gitRepoPath).build())
                                        .build())
                                .build();
                        var gqlResult = client.request(doGitGraphQLQuery);
                        return toRes(gqlResult.executeSync(), graphQlQueryArgs);
                    });
            case AddCodeBranchArgs(String branchName, String gitRepoPath, String sessionKey) ->
                    this.doWithGraphQl(client -> doGitOp(
                            graphQlQueryArgs,
                            client,
                            buildRepoReq(branchName, gitRepoPath, sessionKey, GitOperation.ADD_BRANCH)));
            case AddEmbeddingsArgs(String branchName, String gitRepoPath, String sessionKey) ->
                    this.doWithGraphQl(client -> doGitOp(
                            graphQlQueryArgs,
                            client,
                            buildRepoReq(branchName, gitRepoPath, sessionKey, GitOperation.SET_EMBEDDINGS)));
            case CommitRequestArgs commitRequestArgs ->
                    this.doWithGraphQl(client -> {
                        var gqlResult = client.request(
                                DoCommitGraphQLQuery.newRequest()
                                        .gitRepoPromptingRequest(buildGitRepoPromptingRequest(commitRequestArgs))
                                        .queryName("doCommit")
                                        .build());

                        return toRes(gqlResult.executeSync(), graphQlQueryArgs);
                    });
            default ->
                    throw new IllegalStateException("Unexpected value: " + graphQlQueryArgs);
        };
    }

    private static GitRepositoryRequest buildRepoReq(String branchName, String gitRepoPath, String sessionKey, GitOperation gitOperation) {
        return GitRepositoryRequest.newBuilder()
                .gitBranch(GitBranch.newBuilder().branch(branchName).build())
                .operation(Lists.newArrayList(gitOperation))
                .gitRepo(GitRepo.newBuilder().path(gitRepoPath).build())
                .sessionKey(SessionKey.newBuilder().key(sessionKey).build())
                .build();
    }

    private static GitRepoPromptingRequest buildGitRepoPromptingRequest(CommitRequestArgs commitRequestArgs) {
        CommitMessage cm = CommitMessage.newBuilder().value(commitRequestArgs.commitMessage).build();
        SessionKey session = SessionKey.newBuilder().key(retrieveSessionKey(commitRequestArgs)).build();
        var repoRequest = GitRepoPromptingRequest.newBuilder()
                .gitRepo(GitRepo.newBuilder()
                        .path(commitRequestArgs.gitRepoPath)
                        .build()
                )
                .staged(Staged.newBuilder().diffs(commitRequestArgs.commitDiffContextValue.stagedDiffs())
                        .build())
                .commitMessage(cm)
//                                .ragOptions()
                .sessionKey(session)
                .prev(PrevCommit.newBuilder()
                        .diffs(commitRequestArgs.commitDiffContextValue.prevDiffs())
                        .commitMessage(cm)
                        .sessionKey(session)
                        .build())
                .branchName(commitRequestArgs.branchName)
                .contextData(commitRequestArgs.commitDiffContextValue.getContextData())
                .build();
        return repoRequest;
    }

    private <T> @NotNull Result<T, CommitDiffContextGraphQlError> doGitOp(CallGraphQlQueryArgs<T> graphQlQueryArgs,
                                                                          DgsGraphQlClient client,
                                                                          GitRepositoryRequest sendingCodeBranch) {
        log.info("Sending code branch: {}", sendingCodeBranch);
        var gqlResult = client.request(DoGitGraphQLQuery.newRequest().gitOperation(sendingCodeBranch).queryName("doGit")
                .build());
        var res =  toRes(gqlResult.executeSync(), graphQlQueryArgs);
        return res;
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

    public <T> Result<T, CommitDiffContextGraphQlError> doWithGraphQl(Function<DgsGraphQlClient, Result<T, CommitDiffContextGraphQlError>> toDo) {
        try {
            return toDo.apply(this.graphQlClient);
        } catch (GraphQlTransportException |
                 ResourceAccessException ce) {
            assertions.assertSoftly(false, "Could not connect to graphQL: %s".formatted(ce.getMessage()),
                    "GraphQl transport error: " + ce.getMessage());
            return Result.err(new CommitDiffContextGraphQlError(ce.getMessage()));
        }
    }

    private <T> @NotNull Result<T, CommitDiffContextGraphQlError> toRes(ClientGraphQlResponse gqlResult,
                                                                        CallGraphQlQueryArgs<T> args) {
        var res = Optional.of(gqlResult.toMap())
                .flatMap(s -> Optional.ofNullable(s.get("data")))
                .flatMap(o -> o instanceof Map m ? Optional.of((Map<String, Object>) m) : Optional.empty())
                .flatMap(s -> Optional.ofNullable(s.get(args.key())))
                .flatMap(o -> {
                    try {
                        var read = objectMapper.writeValueAsString(o);
                        var written = objectMapper.readValue(read, args.clazz());
                        return Optional.of(written);
                    } catch (IOException e) {
                        assertions.assertSoftly(false, "Found exception serializing %s"
                                .formatted(args.clazz().getName()), "Successfully serialized.");
                        return Optional.empty();
                    }
                });
        return Result.from(res.orElse(null), getGraphQlError(gqlResult));
    }

}
