package com.hayden.test_graph.commit_diff_context.init.llm_validation;

import com.hayden.commitdiffmodel.codegen.types.GitOperation;
import com.hayden.commitdiffmodel.comittdiff.ParseDiff;
import com.hayden.commitdiffmodel.entity.CommitDiffId;
import com.hayden.commitdiffmodel.git.GitErrors;
import com.hayden.commitdiffmodel.git.GitFactory;
import com.hayden.commitdiffmodel.git.RepoOperations;
import com.hayden.commitdiffmodel.model.GitRefModel;
import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.init.llm_validation.ctx.ValidateLlmInit;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.commit_diff_context.service.CallGraphQlQueryArgs;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.git.RepoUtil;
import com.hayden.utilitymodule.io.ArchiveUtils;
import com.hayden.utilitymodule.result.Result;
import com.hayden.utilitymodule.result.error.SingleError;
import org.assertj.core.util.Files;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class InitializeLlmValidation implements ValidateLlmInitNode {

    @Autowired
    @ResettableThread
    Assertions assertions;
    @Autowired
    ParseDiff parseDiff;
    @Autowired
    GitFactory gitFactory;


    @Override
    @Idempotent
    public ValidateLlmInit exec(ValidateLlmInit c, MetaCtx h) {
        // clone repo, add to context
        var repoOpInit = c.getRepoOpInit();
        var repoData = repoOpInit.repoDataOrThrow();

        try (
                var g = Git.open(repoData.clonedUri().toFile());
                var rh = gitFactory.repositoryHolder(g)
        ) {

            var secondTo = RepoOperations.walkBackwardFromBranch(repoData.branchName(), g)
                    .flatMapResult(iter -> {
                        var iterator = iter.iterator();
                        GitRefModel.NextRevCommit parent;
                        if (iterator.hasNext()) {
                            parent = iterator.next();
                        } else {
                            return Result.err(new GitErrors.GitError("Failed to get second commit."));
                        }
                        if (iterator.hasNext()) {
                            var child = iterator.next();
                            return Result.ok(CommitDiffId.builder().parentHash(parent.getName()).childHash(child.getName()).build());
                        } else {
                            return Result.err(new GitErrors.GitError("Failed to get second commit."));
                        }
                    });

            assertions.assertSoftly(secondTo.isOk(),
                    "Could not parse repo, did not have enough commits for validation: %s."
                            .formatted(secondTo.errorMessage()));

            secondTo.ifPresent(nrc -> {
                // parse backwards, get second from back, get commit hash for this
                var parsed = parseDiff.parseDiffItemsToGitDiff(rh, nrc);

                var latestCommit = RepoUtil.getLatestCommit(rh.getGit(), repoData.branchName())
                        .map(RevCommit::getFullMessage)
                        .mapError(re -> new GitErrors.GitAggregateError(re.getMessage()));

                var lst = parsed.toList();

                String s = GitErrors.GitAggregateError.from(parsed.e().toList()).getMessage();
                List<GitErrors.GitError> allErrs = lst.errsList().stream().flatMap(gae -> gae.errors().stream()).toList();

                assertions.assertSoftly(allErrs.isEmpty(), "Was not successful in generating git commit diffs: %s.".formatted(s));

                assertions.assertSoftly(!lst.results().isEmpty(), "Was not successful in retrieving most recent commit message: %s."
                        .formatted(s));

                assertions.assertSoftly(latestCommit.isOk(), "Could not retreive latest commit: %s."
                        .formatted(latestCommit.errorMessage()));

                var lc = latestCommit.orElseRes(null);

                c.getLlmValidationData().swap(new ValidateLlmInit.LlmValidationCommitData(lst.results(), lc));

                // reset it to the previous commit to predict this commit.
                try {
                    var reset = rh.reset().setMode(ResetCommand.ResetType.HARD)
                            .setRef(nrc.getChildHash())
                            .call();
                    assertions.assertSoftly(true, "Could not reset.", "Resetted to %s".formatted(reset));
                } catch (GitAPIException e) {
                    assertions.assertSoftly(false, "Could not reset: %s."
                            .formatted(SingleError.parseStackTraceToString(e)));
                }
            });
        } catch (GitAPIException |
                 IOException e) {
            assertions.assertSoftly(false, "Could not clone repository %s\n%s."
                    .formatted(repoData.url(), SingleError.parseStackTraceToString(e)));
        }
        return c;
    }

}
