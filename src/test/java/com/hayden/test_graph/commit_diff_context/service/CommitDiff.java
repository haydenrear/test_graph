package com.hayden.test_graph.commit_diff_context.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hayden.commitdiffmodel.codegen.client.BranchGraphQLQuery;
import com.hayden.commitdiffmodel.codegen.client.DoCommitGraphQLQuery;
import com.hayden.commitdiffmodel.codegen.client.DoGitGraphQLQuery;
import com.hayden.commitdiffmodel.codegen.types.*;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.client.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
public class CommitDiff {

    @Autowired
    DgsGraphQlClient graphQlClient;
    @Autowired
    @ResettableThread
    Assertions assertions;
    @Autowired
    @ResettableThread
    RepoOpInit repoOpInit;

    @Autowired
    private ObjectMapper objectMapper;

    public <T> Result<T, CallGraphQlQueryArgs.CommitDiffContextGraphQlError> callGraphQlQuery(CallGraphQlQueryArgs<T> graphQlQueryArgs) {
        return switch (graphQlQueryArgs) {
            case CallGraphQlQueryArgs.ValidateBranchAdded(String branchName, String gitRepoPath) ->
                    this.doWithGraphQl(client -> toRes(
                            client.request(
                                            BranchGraphQLQuery.newRequest()
                                                    .queryName(graphQlQueryArgs.key())
                                                    .gitRepo(GitRepoQueryRequest.newBuilder()
                                                            .gitBranch(GitBranch.newBuilder().branch(branchName).build())
                                                            .gitRepo(GitRepo.newBuilder().path(gitRepoPath).build())
                                                            .build())
                                                    .build())
                                    .executeSync(),
                            graphQlQueryArgs));
            case CallGraphQlQueryArgs.AddCodeBranchArgs(String branchName, String gitRepoPath, String sessionKey) ->
                    this.doWithGraphQl(client -> doGitOp(
                            graphQlQueryArgs,
                            client,
                            buildRepoReq(branchName, gitRepoPath, sessionKey, GitOperation.ADD_BRANCH)));
            case CallGraphQlQueryArgs.AddEmbeddingsArgs(String branchName, String gitRepoPath, String sessionKey) ->
                    this.doWithGraphQl(client -> doGitOp(
                            graphQlQueryArgs,
                            client,
                            buildRepoReq(branchName, gitRepoPath, sessionKey, GitOperation.SET_EMBEDDINGS)));
            case CallGraphQlQueryArgs.CommitRequestArgs commitRequestArgs ->
                    this.doWithGraphQl(client -> {
                        var gqlResult = client.request(
                                DoCommitGraphQLQuery.newRequest()
                                        .gitRepoPromptingRequest(buildGitRepoPromptingRequest(commitRequestArgs))
                                        .queryName(commitRequestArgs.key())
                                        .build());

                        return toRes(gqlResult.executeSync(), graphQlQueryArgs);
                    });
            default ->
                    throw new IllegalStateException("Unexpected value: " + graphQlQueryArgs);
        };
    }

    private GitRepositoryRequest buildRepoReq(String branchName, String gitRepoPath,
                                              String sessionKey, GitOperation gitOperation) {
        assertions.assertSoftly(sessionKey.equals(this.repoOpInit.retrieveSessionKey()),
                "Session key did not propagate.",
                "Session key existed as %s".formatted(sessionKey));
        return GitRepositoryRequest.newBuilder()
                .gitBranch(GitBranch.newBuilder().branch(branchName).build())
                .operation(Lists.newArrayList(gitOperation))
                .gitRepo(GitRepo.newBuilder().path(gitRepoPath).build())
                .sessionKey(SessionKey.newBuilder().key(sessionKey).build())
                .build();
    }

    private GitRepoPromptingRequest buildGitRepoPromptingRequest(CallGraphQlQueryArgs.CommitRequestArgs commitRequestArgs) {
        CommitMessage cm = CommitMessage.newBuilder().value(commitRequestArgs.commitMessage()).build();
        SessionKey session = SessionKey.newBuilder().key(repoOpInit.retrieveSessionKey()).build();
        var repoRequest = GitRepoPromptingRequest.newBuilder()
                .gitRepo(GitRepo.newBuilder()
                        .path(commitRequestArgs.gitRepoPath())
                        .build())
                .staged(Staged.newBuilder()
                        .diffs(commitRequestArgs.commitDiffContextValue().stagedDiffs())
                        .build())
                .commitMessage(cm)
                .ragOptions(commitRequestArgs.commitDiffContextValue().ragOptions())
                .sessionKey(session)
                .prev(PrevCommit.newBuilder()
                        .diffs(commitRequestArgs.commitDiffContextValue().prevDiffs())
                        .commitMessage(cm)
                        .sessionKey(session)
                        .build())
                .branchName(commitRequestArgs.branchName())
                .contextData(commitRequestArgs.commitDiffContextValue().getContextData())
                .build();
        return repoRequest;
    }

    private <T> @NotNull Result<T, CallGraphQlQueryArgs.CommitDiffContextGraphQlError> doGitOp(CallGraphQlQueryArgs<T> graphQlQueryArgs,
                                                                                               DgsGraphQlClient client,
                                                                                               GitRepositoryRequest sendingCodeBranch) {
        log.info("Sending code branch: {}", sendingCodeBranch);
        var gqlResult = client.request(
                DoGitGraphQLQuery.newRequest()
                        .repoRequest(sendingCodeBranch)
                        .queryName(graphQlQueryArgs.key())
                        .build());
        var res =  toRes(gqlResult.executeSync(), graphQlQueryArgs);
        return res;
    }

    public <T> Result<T, CallGraphQlQueryArgs.CommitDiffContextGraphQlError> doWithGraphQl(Function<DgsGraphQlClient, Result<T, CallGraphQlQueryArgs.CommitDiffContextGraphQlError>> toDo) {
        try {
            return toDo.apply(this.graphQlClient);
        } catch (GraphQlTransportException |
                 ResourceAccessException ce) {
            assertions.assertSoftly(false, "Could not connect to graphQL: %s".formatted(ce.getMessage()),
                    "GraphQl transport error: " + ce.getMessage());
            return Result.err(new CallGraphQlQueryArgs.CommitDiffContextGraphQlError(ce.getMessage()));
        }
    }

    private <T> @NotNull Result<T, CallGraphQlQueryArgs.CommitDiffContextGraphQlError> toRes(ClientGraphQlResponse gqlResult,
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

    private static @NotNull CallGraphQlQueryArgs.CommitDiffContextGraphQlError getGraphQlError(ClientGraphQlResponse clientGraphQlResponse) {
        return new CallGraphQlQueryArgs.CommitDiffContextGraphQlError(clientGraphQlResponse.getErrors());
    }

}
