package com.hayden.test_graph.commit_diff_context.step_def;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hayden.commitdiffcontext.code_apply.DiffFactory;
import com.hayden.commitdiffcontext.context.ParseDiff;
import com.hayden.commitdiffcontext.context.StagedFileService;
import com.hayden.commitdiffcontext.context.validation.entity.CommitDiffContextCommitVersion;
import com.hayden.commitdiffcontext.git.entity.GitDiffs;
import com.hayden.commitdiffmodel.codegen.types.RelevantFileItem;
import com.hayden.commitdiffmodel.codegen.types.RelevantFileItems;
import com.hayden.commitdiffmodel.codegen.types.Staged;
import com.hayden.commitdiffcontext.convert.CommitDiffContextMapper;
import com.hayden.commitdiffmodel.err.GitErrors;
import com.hayden.commitdiffcontext.git.GitFactory;
import com.hayden.proto.prototyped.datasources.ai.modelserver.client.ModelServerValidationAiClient;
import com.hayden.proto.prototyped.datasources.ai.modelserver.request.ModelServerChatRequest;
import com.hayden.proto.prototyped.datasources.ai.modelserver.request.RetryParameters;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.next_commit.NextCommitAssert;
import com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op.RepoOpAssertCtx;
import com.hayden.commitdiffmodel.config.CommitDiffContextConfigProps;
import com.hayden.utilitymodule.db.DbDataSourceTrigger;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.commit_diff_context.init.llm_validation.ctx.ValidateLlmInit;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.steps.*;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.io.FileUtils;
import com.hayden.utilitymodule.result.Result;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

@Slf4j
public class LlmValidationNextCommit implements ResettableStep {

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
    @ResettableThread
    NextCommitAssert nextCommit;
    @Autowired
    @ResettableThread
    ValidateLlmInit validateLlmInit;

    @Autowired
    CommitDiffContextMapper mapper;
    @Autowired
    ModelServerValidationAiClient validationAiClient;
    @Autowired
    CommitDiffContextConfigProps props;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    com.hayden.commitdiffcontext.context.validation.repo.CommitDiffContextVersionRepo versionRepo;
    @Autowired
    DbDataSourceTrigger dbDataSourceTrigger;
    @Autowired
    ParseDiff parseDiff;
    @Autowired
    StagedFileService stagedFileService;
    @Autowired
    GitFactory gitArgsFactory;

    @Autowired
    @ResettableThread
    CommitDiffInit init;

    @Given("a postgres database to be loaded from {string} for docker-compose {string}")
    @RegisterInitStep(RepoOpInit.class)
    public void startPostgresDatabase(String postgresSource, String dockerCompose) {
        init.getSkipCleanupNode().swap(true);
        final Path postgresSrcDir = getPostgresSrcDir(postgresSource);
        assertions.assertSoftly(postgresSrcDir.toFile().exists() || postgresSrcDir.toFile().mkdirs(),
                "Postgres directory %s exists or was able to be created.".formatted(postgresSource));

        final Path dockerComposeFile = getDockerFileSave(dockerCompose);

        var written = FileUtils.readToString(dockerComposeFile.resolve("docker-compose-template.yml").toFile())
                .mapError(se -> new FileUtils.FileError(se.getMessage()))
                .flatMapResult(found -> {
                    var replaced = found.replaceAll("\\{\\{postgres_source}}", postgresSrcDir.toString());
                    return FileUtils.writeToFileRes(replaced, dockerComposeFile.resolve("docker-compose.yml"));
                })
                .one();

        assertions.assertSoftly(written.isOk(), "Was not successfully generating docker-compose with err: %s"
                .formatted(written.errorMessage()));
        written.ifPresent(didSuccessfullyWriteDockerCompose -> {
            assertions.assertSoftly(didSuccessfullyWriteDockerCompose, "Was not successfuly generating docker-compose");
            assertions.assertSoftly(dockerComposeFile.toFile().exists(), "docker-compose file did not exist.");

            if (didSuccessfullyWriteDockerCompose) {
                assertions.reportAssert("Wrote docker-compose file to %s", dockerComposeFile);
                dockerInitCtx.composePath().swap(dockerComposeFile.toFile());
            }
        });

    }

    private @NotNull Path getPostgresSrcDir(String postgresSource) {
        var p = FileUtils.replaceHomeDir(Paths.get(props.getHomeDir()), postgresSource).toPath().toAbsolutePath();
        return p;
    }

    private @NotNull Path getDockerFileSave(String dockerCompose) {
        var p = FileUtils.replaceHomeDir(Paths.get(props.getHomeDir()), dockerCompose).toPath().toAbsolutePath();
        return p;
    }

    /**
     * Add git diffs to memory for current most recent commit, then reset to previous commit to prepare for prediction of that commit being removed.
     */
    @And("the most recent commit is saved to memory and removed from the repository")
    @RegisterInitStep(ValidateLlmInit.class)
    public void addMostRecentCommitInfo() {
        log.info("Registered to save most recent commit and reset to previous.");
    }

    @Then("the validation data is saved for review")
    @ExecAssertStep({RepoOpAssertCtx.class, NextCommitAssert.class})
    public void theValidationScoreIsSavedToFileForReview() {
        nextCommit.getValidationResponse().optional()
                .flatMap(res -> Optional.ofNullable(res.response()))
                .flatMap(mrc -> Optional.ofNullable(mrc.validationResult()))
                .or(() -> {
                    assertions.assertSoftly(false, "Validation result was not set.");
                    return Optional.empty();
                })
                .filter(Predicate.not(t -> t.validationScore() == -1f))
                .or(() -> {
                    assertions.assertSoftly(false, "Validation score was non-initialized.");
                    return Optional.empty();
                })
                .flatMap(vrc -> repoOpInit.repoData().optional()
                        .map(rd -> Map.entry(vrc, rd)))
                .flatMap(vrc -> nextCommit.getActualCommitInfo().optional()
                        .or(() -> {
                            assertions.assertSoftly(false, "Actual commit infor was not set.");
                            return Optional.empty();
                        })
                        .map(assertedGitDiffs -> Map.entry(assertedGitDiffs, vrc)))
                .ifPresentOrElse(nextCommitMrc -> {
                            CommitDiffContextCommitVersion.AssertedGitDiffs asserted = nextCommitMrc.getKey();
                            Map.Entry<ModelServerValidationAiClient.ValidationResult, RepoOpInit.RepositoryData> mrc = nextCommitMrc.getValue();
                            var dataSourceKey = this.dbDataSourceTrigger.doWithKey(setKey -> {
                                setKey.setInitialized();
                                var saved = doSaveVersionRepo(mrc, asserted);
                                setKey.setInit();
                                var savedValidation = doSaveVersionRepo(mrc, asserted);
                            });
                            assertions.assertSoftly(Objects.equals(dataSourceKey, DbDataSourceTrigger.APP_DB_KEY),
                                    "DataSource trigger produced the wrong key.");
                        },
                        () -> assertions.assertSoftly(false, "Validation was not received from server."));
    }

    private @NotNull CommitDiffContextCommitVersion doSaveVersionRepo(Map.Entry<ModelServerValidationAiClient.ValidationResult, RepoOpInit.RepositoryData> mrc,
                                                                      CommitDiffContextCommitVersion.AssertedGitDiffs asserted) {
        var saved = versionRepo.save(
                CommitDiffContextCommitVersion.builder()
                        .branch(mrc.getValue().branchName())
                        .repo(mrc.getValue().url())
                        .parsed(asserted)
                        .validationScore(mrc.getKey().validationScore())
                        .build());
        assertions.assertSoftly(Objects.nonNull(saved.getUuid()),
                "Validation score as not saved for %s."
                        .formatted(mrc.getValue()),
                "Validation score was set for %s."
                        .formatted(mrc.getValue()));
        return saved;
    }

    @Then("postgres database should be started")
    @ExecInitStep({RepoOpInit.class})
    public void postgresDatabaseShouldBeStarted() {

    }

    @And("the staged commit information is retrieved from the repository")
    public void theStagedCommitInformationIsRetrievedFromTheRepository() {
        var gitRepoPromptingRequest = repoOpInit.getCommitDiffContextValue();
        RepoOpInit.RepositoryData repositoryData = repoOpInit.repoDataOrThrow();
        try{
            var rh = gitArgsFactory.stripedRepoHolder(GitFactory.parseRepoArgs(repositoryData.url(), repositoryData.branchName(), repositoryData.clonedUri()));
            stagedFileService.getStagedChanges(rh)
                    .ifPresent(staged -> {
                        var s = Staged.newBuilder()
                                .diffs(staged.staged().getDiffs())
                                .files(staged.diffFiles().stream()
                                        .map(r -> {
                                            List<String> before = r.fileBeforeDiffApply().relevantChosenLinesWithLineNumbers();
                                            List<String> after = r.fileAfterDiffApply().relevantChosenLinesWithLineNumbers();
                                            return new RelevantFileItems(
                                                    new RelevantFileItem(r.fileBeforeDiffApply().fileName().toAbsolutePath().toString(), before.isEmpty() ? "": before.getFirst()),
                                                    new RelevantFileItem(r.fileAfterDiffApply().fileName().toAbsolutePath().toString(), after.isEmpty() ? "": after.getFirst()));
                                        })
                                        .toList()
                                )
                                .build();
                        gitRepoPromptingRequest.nextCommitRequest()
                                .setLastRequestStagedApplied(s);
                    });
            rh.reset().setMode(ResetCommand.ResetType.HARD).setRef("HEAD").call();
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
