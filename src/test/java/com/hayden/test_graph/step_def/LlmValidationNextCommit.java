package com.hayden.test_graph.step_def;


import com.hayden.commitdiffmodel.comittdiff.ParseDiff;
import com.hayden.commitdiffmodel.convert.CommitDiffContextMapper;
import com.hayden.commitdiffmodel.entity.CommitDiffId;
import com.hayden.commitdiffmodel.git.GitErrors;
import com.hayden.commitdiffmodel.git.RepoOperations;
import com.hayden.commitdiffmodel.git.RepositoryHolder;
import com.hayden.commitdiffmodel.model.GitRefModel;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.io.FileUtils;
import com.hayden.utilitymodule.result.Result;
import com.hayden.utilitymodule.result.error.SingleError;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.assertj.core.util.Files;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.util.FS;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LlmValidationNextCommit {

    @Autowired
    @ResettableThread
    Assertions assertions;
    @Autowired
    @ResettableThread
    DockerInitCtx dockerInitCtx;
    @Autowired
    @ResettableThread
    RepoOpInit repoOpInit;

    @Autowired
    ParseDiff parseDiff;
    @Autowired
    CommitDiffContextMapper mapper;

    @Given("a postgres database to be loaded from {string} for docker-compose {string}")
    public void startPostgresDatabase(String postgresSource, String dockerCompose) {
        final Path dockerComposeFile = Paths.get(dockerCompose, "docker-compose.yml");
        var written = FileUtils.readToString(Paths.get(dockerCompose, "docker-compose-template.yml").toFile())
                .mapError(se -> new FileUtils.FileError(se.getMessage()))
                .flatMapResult(found -> {
                    var replaced = found.replaceAll("\\{\\{postgres_source}}", postgresSource);
                    return FileUtils.writeToFileRes(replaced, dockerComposeFile);
                })
                .one();

        assertions.assertSoftly(written.isOk(), "Was not successfuly generating docker-compose");
        written.ifPresent(b -> {
            assertions.assertSoftly(b, "Was not successfuly generating docker-compose");
            assertions.assertSoftly(dockerComposeFile.toFile().exists(), "docker-compose file did not exist.");

            if (b)
                dockerInitCtx.composePath().swap(dockerComposeFile.toFile());
        });

    }

    /**
     * Add git diffs to memory for current most recent commit, then reset to previous commit to prepare for prediction.
     */
    @And("the most recent commit is saved to memory and removed from the repository")
    public void addMostRecentCommitInfo() {
        var temp = Files.newTemporaryFolder();

        var repoData = repoOpInit.repoDataOrThrow();

        repoOpInit.repoData().swap(repoData.withClonedUri(temp.toPath()));

        try(var g = Git.cloneRepository().setBranch(repoData.branchName())
                    .setURI(repoData.url())
                    .setDirectory(repoData.clonedUri().toFile())
                    .setFs(FS.detect())
                    .call();
            var rh = new RepositoryHolder(g, mapper)
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
                            .formatted(secondTo.e().map(GitErrors.GitError::getMessage).orElse(null)));

            secondTo.ifPresent(nrc -> {
                // parse backwards, get second from back, get commit hash for this
                var parsed = parseDiff.parseDiffItemsToGitDiff(() -> parseDiff.retrieveDiffEntries(nrc, rh), rh);

                assertions.assertSoftly(parsed.isOk(), "Was not successful in generating git commit diffs: %s."
                        .formatted(parsed.e().map(GitErrors.GitAggregateError::getMessage).orElse(null)));

                parsed.map(ParseDiff.GitDiffResult::diffs)
                        .ifPresent(gdr -> repoOpInit.getLlmValidationData().swap(new RepoOpInit.LlmValidationCommitData(gdr)));

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
        } catch (GitAPIException | IOException e) {
            assertions.assertSoftly(false, "Could not clone repository %s\n%s."
                    .formatted(repoData.url(), SingleError.parseStackTraceToString(e)));
        }



    }
}
