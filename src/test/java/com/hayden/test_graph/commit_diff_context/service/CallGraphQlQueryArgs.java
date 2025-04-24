package com.hayden.test_graph.commit_diff_context.service;

import com.hayden.commitdiffmodel.codegen.types.*;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.utilitymodule.result.error.SingleError;
import lombok.Builder;
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
    record CodeContextQueryArgs(String branchName, String gitRepoPath, String commitMessage,
                             RepoOpInit.CommitDiffContextGraphQlModel commitDiffContextValue) implements CallGraphQlQueryArgs<CommitDiffFileResult> {
        @Override
        public Class<CommitDiffFileResult> clazz() {
            return CommitDiffFileResult.class;
        }

        @Override
        public String key() {
            return "buildCommitDiffContext";
        }
    }

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
    record DoGitArgs(String branchName, String gitRepoPath, String sessionKey, List<GitOperation> gitOperation, Object ... ctx) implements CallGraphQlQueryArgs<GitRepoResult> {

        public DoGitArgs(String branchName, String gitRepoPath, String sessionKey, List<GitOperation> gitOperation, Object ... ctx) {
            this.branchName = branchName;
            this.gitRepoPath = gitRepoPath;
            this.sessionKey = sessionKey(sessionKey) ;
            this.gitOperation = gitOperation;
            this.ctx = ctx;
        }

        public DoGitArgs(String branchName, String gitRepoPath, String sessionKey, GitOperation gitOperation) {
            this(branchName, gitRepoPath, sessionKey, List.of(gitOperation), (Object) null);
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

    static GitRepositoryRequest doBuildRepoRequest(String branchName, String gitRepoPath,
                                                   String sessionKey, List<GitOperation> gitOperation,
                                                   Object[] ctxs, RepoOpInit repoOpInit) {
        var ragOptions = repoOpInit.doGitRagOptions();
        var gReq = GitRepositoryRequest.newBuilder();
        gReq = gReq.operation(gitOperation)
                .gitBranch(GitBranch.newBuilder().branch(branchName).build())
                .gitRepo(GitRepo.newBuilder().path(gitRepoPath).build())
                .ragOptions(ragOptions)
                .sessionKey(SessionKey.newBuilder().key(sessionKey).build());
        for (var ctx : ctxs) {
            gReq = addCtx(ctx, gReq);
        }
        return gReq.build();
    }

    static GitRepositoryRequest.Builder addCtx(Object ctx, GitRepositoryRequest.Builder b) {
        return switch (ctx) {
            case TagContext t -> b.tag(t);
            case MergeContext m -> b.merge(m);
            case RebaseContext r -> b.rebase(r);
            case CherryPickContext r -> b.cherryPick(r);
            case ResetContext r -> b.reset(r);
            case DropContext r -> b.drop(r);
            case UpdateHeadCtx r ->b.updateHead(r);
            case null, default -> b;
        };
    }

}
