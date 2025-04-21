package com.hayden.test_graph.commit_diff_context.service;

import com.hayden.commitdiffmodel.codegen.types.*;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.utilitymodule.result.error.SingleError;
import lombok.Builder;
import org.assertj.core.util.Lists;
import org.springframework.graphql.ResponseError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public sealed interface CallGraphQlQueryArgs<T> {
    Class<T> clazz();

    String key();

    @Builder
    record ValidateBranchAdded(String branchName, String gitRepoPath) implements CallGraphQlQueryArgs<GitRepoResult> {
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
    record DoGitArgs(String branchName, String gitRepoPath, String sessionKey, GitOperation gitOperation, Object ctx) implements CallGraphQlQueryArgs<GitRepoResult> {

        public DoGitArgs(String branchName, String gitRepoPath, String sessionKey, GitOperation gitOperation, Object ctx) {
            this.branchName = branchName;
            this.gitRepoPath = gitRepoPath;
            this.sessionKey = sessionKey(sessionKey) ;
            this.gitOperation = gitOperation;
            this.ctx = ctx;
        }

        public DoGitArgs(String branchName, String gitRepoPath, String sessionKey, GitOperation gitOperation) {
            this(branchName, gitRepoPath, sessionKey, gitOperation, null);
        }

        @Override
        public Class<GitRepoResult> clazz() {
            return GitRepoResult.class;
        }

        @Override
        public String key() {
            return "doGit";
        }

        public static String sessionKey(String sessionKey) {
            return Optional.ofNullable(sessionKey)
                    .orElse(UUID.randomUUID().toString());
        }
    }

    @Builder
    record CommitRequestArgs(String branchName, String gitRepoPath, String commitMessage,
                             RepoOpInit.CommitDiffContextGraphQlModel commitDiffContextValue) implements CallGraphQlQueryArgs<NextCommit> {
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
    record CommitDiffContextGraphQlError(List<ResponseError> errors, String error) implements SingleError {

        public CommitDiffContextGraphQlError(List<ResponseError> errors) {
            this(errors, null);
        }

        public CommitDiffContextGraphQlError(String errors) {
            this(new ArrayList<>(), errors);
        }

        @Override
        public String getMessage() {
            return errors.stream()
                    .map(re -> "%s: %s".formatted(re.toString(), re.getMessage()))
                    .collect(Collectors.joining(", "));
        }
    }

    static GitRepositoryRequest toRepoRequest(String branchName, String gitRepoPath,
                                              String sessionKey, GitOperation gitOperation,
                                              Object ctx, RepoOpInit repoOpInit) {
        var ragOptions = repoOpInit.doGitRagOptions();
        return switch (ctx) {
            case TagContext t ->
                    GitRepositoryRequest.newBuilder()
                            .gitBranch(GitBranch.newBuilder().branch(branchName).build())
                            .operation(Lists.newArrayList(gitOperation))
                            .gitRepo(GitRepo.newBuilder().path(gitRepoPath).build())
                            .ragOptions(ragOptions)
                            .sessionKey(SessionKey.newBuilder().key(sessionKey).build())
                            .tag(t)
                            .build();
            case MergeContext m ->
                    GitRepositoryRequest.newBuilder()
                            .gitBranch(GitBranch.newBuilder().branch(branchName).build())
                            .operation(Lists.newArrayList(gitOperation))
                            .gitRepo(GitRepo.newBuilder().path(gitRepoPath).build())
                            .ragOptions(ragOptions)
                            .sessionKey(SessionKey.newBuilder().key(sessionKey).build())
                            .merge(m)
                            .build();
            case RebaseContext r ->
                    GitRepositoryRequest.newBuilder()
                            .gitBranch(GitBranch.newBuilder().branch(branchName).build())
                            .operation(Lists.newArrayList(gitOperation))
                            .gitRepo(GitRepo.newBuilder().path(gitRepoPath).build())
                            .ragOptions(ragOptions)
                            .sessionKey(SessionKey.newBuilder().key(sessionKey).build())
                            .rebase(r)
                            .build();
            case CherryPickContext r ->
                    GitRepositoryRequest.newBuilder()
                            .gitBranch(GitBranch.newBuilder().branch(branchName).build())
                            .operation(Lists.newArrayList(gitOperation))
                            .gitRepo(GitRepo.newBuilder().path(gitRepoPath).build())
                            .ragOptions(ragOptions)
                            .sessionKey(SessionKey.newBuilder().key(sessionKey).build())
                            .cherryPick(r)
                            .build();
            case ResetContext r ->
                    GitRepositoryRequest.newBuilder()
                            .gitBranch(GitBranch.newBuilder().branch(branchName).build())
                            .operation(Lists.newArrayList(gitOperation))
                            .gitRepo(GitRepo.newBuilder().path(gitRepoPath).build())
                            .sessionKey(SessionKey.newBuilder().key(sessionKey).build())
                            .reset(r)
                            .build();
            case DropContext r ->
                    GitRepositoryRequest.newBuilder()
                            .gitBranch(GitBranch.newBuilder().branch(branchName).build())
                            .operation(Lists.newArrayList(gitOperation))
                            .gitRepo(GitRepo.newBuilder().path(gitRepoPath).build())
                            .ragOptions(ragOptions)
                            .sessionKey(SessionKey.newBuilder().key(sessionKey).build())
                            .drop(r)
                            .build();
            case UpdateHeadCtx r ->
                    GitRepositoryRequest.newBuilder()
                            .gitBranch(GitBranch.newBuilder().branch(branchName).build())
                            .operation(Lists.newArrayList(gitOperation))
                            .gitRepo(GitRepo.newBuilder().path(gitRepoPath).build())
                            .ragOptions(ragOptions)
                            .sessionKey(SessionKey.newBuilder().key(sessionKey).build())
                            .updateHead(r)
                            .build();
            case null, default ->
                    GitRepositoryRequest.newBuilder()
                            .gitBranch(GitBranch.newBuilder().branch(branchName).build())
                            .operation(Lists.newArrayList(gitOperation))
                            .gitRepo(GitRepo.newBuilder().path(gitRepoPath).build())
                            .ragOptions(ragOptions)
                            .sessionKey(SessionKey.newBuilder().key(sessionKey).build())
                            .build();
        };
    }
}
