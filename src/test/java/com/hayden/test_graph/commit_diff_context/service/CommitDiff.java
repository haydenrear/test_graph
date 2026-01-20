package com.hayden.test_graph.commit_diff_context.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hayden.commitdiffcontext.context.GitGraphQlProjections;
import com.hayden.commitdiffmodel.codegen.client.*;
import com.hayden.commitdiffmodel.codegen.types.*;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.test_graph.thread.ResettableThreadLike;
import com.hayden.utilitymodule.result.Result;
import com.hayden.utilitymodule.result.error.SingleError;
import com.netflix.graphql.dgs.client.codegen.BaseProjectionNode;
import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.client.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static com.hayden.test_graph.commit_diff_context.service.CallGraphQlQueryArgs.doBuildRepoRequest;

@Slf4j
@Service
public class CommitDiff implements ResettableThreadLike {

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
                    this.doWithGraphQl(client -> callValidateBranch(graphQlQueryArgs, branchName, gitRepoPath, client));
            case CallGraphQlQueryArgs.DoGitArgs(String branchName, String gitRepoPath, String sessionKey, List<GitOperation> doGitOp, Object[] ctx) -> {
                RepoOpInit.CommitDiffContextGraphQlModel commitDiffContextGraphQlModel = repoOpInit.getCommitDiffContextValue();
                Boolean async = commitDiffContextGraphQlModel.nextCommitRequest().getAsync();
                if (async) {
                    Integer numSecondsWait = commitDiffContextGraphQlModel.numSecondsAsync().optional().orElse(5);
                    assertions.reportAssert("Performing doGit asynchronously, will wait " + numSecondsWait + " seconds");
                    yield this.doWithGraphQlAsync(client -> doGitOp(graphQlQueryArgs, client, buildRepoReq(branchName, gitRepoPath, sessionKey, doGitOp, ctx)),
                            numSecondsWait);
                } else {
                    yield this.doWithGraphQl(client -> doGitOp(graphQlQueryArgs, client, buildRepoReq(branchName, gitRepoPath, sessionKey, doGitOp, ctx)));
                }
            }
        };
    }

    private <T> @NotNull Result<T, CallGraphQlQueryArgs.CommitDiffContextGraphQlError> callValidateBranch(CallGraphQlQueryArgs<T> graphQlQueryArgs, String branchName, String gitRepoPath, DgsGraphQlClient client) {
        DgsGraphQlClient.RequestSpec requestSpec = doCreateRequestSpec(
                client,
                BranchGraphQLQuery.newRequest()
                        .queryName(graphQlQueryArgs.key())
                        .gitRepo(
                                GitRepoQueryRequest.newBuilder()
                                        .gitBranch(GitBranch.newBuilder().branch(branchName).build())
                                        .gitRepo(GitRepo.newBuilder().path(gitRepoPath).build())
                                        .build())
                        .build(),
                new BranchProjectionRoot<>().branch());

        return createGraphQlQueryResponse(requestSpec.executeSync(), graphQlQueryArgs);
    }


    private <T> @NotNull Result<T, CallGraphQlQueryArgs.CommitDiffContextGraphQlError> doGitOp(CallGraphQlQueryArgs<T> graphQlQueryArgs,
                                                                                               DgsGraphQlClient client,
                                                                                               GitRepositoryRequest sendingCodeBranch) {
        log.info("Sending code branch: {}", sendingCodeBranch);
        DoGitGraphQLQuery built = DoGitGraphQLQuery.newRequest()
                .repoRequest(sendingCodeBranch)
                .queryName(graphQlQueryArgs.key())
                .build();
        var projectionNode = new DoGitProjectionRoot<>().branch().sessionKey().key().getParent();
        var req = doCreateRequestSpec(client, built, projectionNode);
        var res = createGraphQlQueryResponse(req.executeSync(), graphQlQueryArgs);
        return res;
    }

    private GitRepositoryRequest buildRepoReq(String branchName, String gitRepoPath,
                                              String sessionKey, List<GitOperation> gitOperation,
                                              Object ... ctx) {
        assertions.assertSoftly(sessionKey.equals(
                        this.repoOpInit.retrieveSessionKey()),
                "Session key did not propagate.",
                "Session key existed as %s".formatted(sessionKey));
        return doBuildRepoRequest(branchName, gitRepoPath, sessionKey, gitOperation, ctx, repoOpInit);
    }

    private DgsGraphQlClient.@NotNull RequestSpec doCreateRequestSpec(DgsGraphQlClient client,
                                                                      GraphQLQuery query,
                                                                      BaseProjectionNode projection) {
        var serializedQuery = new GraphQLQueryRequest(query, projection).serialize();
        assertions.reportAssert("GraphQl query to be executed: %s".formatted(serializedQuery));
        var rs = client.request(query).projection(projection);
        return rs;
    }

    private GitRepoPromptingRequest buildGitRepoPromptingRequestInner(String commitMessage,
                                                                      RepoOpInit.CommitDiffContextGraphQlModel commitDiffContextGraphQlModel,
                                                                      String branchName, String path) {
        CommitMessage cm = CommitMessage.newBuilder().value(Optional.ofNullable(commitMessage).orElse("")).build();
        SessionKey session = SessionKey.newBuilder().key(repoOpInit.retrieveSessionKey()).build();
        var repoRequest = GitRepoPromptingRequest.newBuilder()
                .gitRepoRequestOptions(commitDiffContextGraphQlModel.nextCommitRequest().getGitRepoRequestOptions())
                .gitRepo(GitRepo.newBuilder()
                        .path(path)
                        .build())
                .lastRequestStagedApplied(commitDiffContextGraphQlModel.nextCommitRequest().getLastRequestStagedApplied())
                .staged(Staged.newBuilder()
                        .diffs(commitDiffContextGraphQlModel.stagedDiffs())
                        .build())
                .commitMessage(cm)
                .ragOptions(commitDiffContextGraphQlModel.nextCommitRequest().getRagOptions())
                .sessionKey(session)
                .prev(PrevCommit.newBuilder()
                        .diffs(commitDiffContextGraphQlModel.prevDiffs())
                        .commitMessage(cm)
                        .sessionKey(session)
                        .build())
                .branchName(branchName)
                .contextData(commitDiffContextGraphQlModel.getContextData())
                .build();
        return repoRequest;
    }

    @SneakyThrows
    public <T> Result<T, CallGraphQlQueryArgs.CommitDiffContextGraphQlError> doWithGraphQlAsync(Function<DgsGraphQlClient, Result<T, CallGraphQlQueryArgs.CommitDiffContextGraphQlError>> toDo,
                                                                                                int numSecondsWait) {
        return doWithGraphQl(toDo)
                .flatMapResult(f -> {
                    try {
                        Thread.sleep(numSecondsWait * 1000L);
                        return Result.ok(f);
                    } catch (
                            InterruptedException e) {
                        return Result.err(new CallGraphQlQueryArgs.CommitDiffContextGraphQlError("Could not wait for finished: %s.".formatted(SingleError.parseStackTraceToString(e))));
                    }
                });
    }

    public <T> Result<T, CallGraphQlQueryArgs.CommitDiffContextGraphQlError> doWithGraphQl(Function<DgsGraphQlClient, Result<T, CallGraphQlQueryArgs.CommitDiffContextGraphQlError>> toDo) {
        try {
            return toDo.apply(this.graphQlClient);
        } catch (GraphQlTransportException | ResourceAccessException ce) {
            assertions.assertSoftly(false, "Could not connect to graphQL: %s".formatted(ce.getMessage()),
                    "GraphQl transport error: " + ce.getMessage());
            return Result.err(new CallGraphQlQueryArgs.CommitDiffContextGraphQlError(ce.getMessage()));
        }
    }

    private <T> @NotNull Result<T, CallGraphQlQueryArgs.CommitDiffContextGraphQlError> createGraphQlQueryResponse(ClientGraphQlResponse gqlResult,
                                                                                                                  CallGraphQlQueryArgs<T> args) {
        var res = Optional.of(gqlResult.toMap())
                .flatMap(s -> Optional.ofNullable(s.get("data")))
                .flatMap(o -> o instanceof Map m ? Optional.of((Map<String, Object>) m) : Optional.empty())
                .flatMap(s -> Optional.ofNullable(s.get(args.key())))
                .map(o -> {
                    try {
                        var read = objectMapper.writeValueAsString(o);
                        var written = objectMapper.readValue(read, args.clazz());
                        return Result.<T, CallGraphQlQueryArgs.CommitDiffContextGraphQlError>ok(written);
                    } catch (IOException e) {
                        assertions.assertSoftly(false, "Found exception serializing %s"
                                .formatted(args.clazz().getName()), "Successfully serialized.");
                        return Result.<T, CallGraphQlQueryArgs.CommitDiffContextGraphQlError>err(
                                new CallGraphQlQueryArgs.CommitDiffContextGraphQlError(SingleError.parseStackTraceToString(e)));
                    }
                });

        return res.orElseGet(() -> Result.err(getGraphQlError(gqlResult)));

    }

    private static @NotNull CallGraphQlQueryArgs.CommitDiffContextGraphQlError getGraphQlError(ClientGraphQlResponse clientGraphQlResponse) {
        return new CallGraphQlQueryArgs.CommitDiffContextGraphQlError(clientGraphQlResponse.getErrors());
    }

}
