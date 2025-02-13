package com.hayden.test_graph.commit_diff_context.service;

import com.hayden.commitdiffmodel.codegen.types.GitRepoResult;
import com.hayden.commitdiffmodel.codegen.types.NextCommit;
import com.hayden.test_graph.commit_diff_context.init.mountebank.CdMbInitBubbleCtx;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.utilitymodule.result.error.SingleError;
import lombok.Builder;
import org.springframework.graphql.ResponseError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public interface CallGraphQlQueryArgs<T> {
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
    record AddCodeBranchArgs(String branchName, String gitRepoPath, String sessionKey) implements CallGraphQlQueryArgs<GitRepoResult> {
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
    record AddEmbeddingsArgs(String branchName, String gitRepoPath, String sessionKey)
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
}
