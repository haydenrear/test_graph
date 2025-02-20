package com.hayden.test_graph.commit_diff_context.step_def;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hayden.commitdiffmodel.comittdiff.ParseDiff;
import com.hayden.commitdiffmodel.convert.CommitDiffContextMapper;
import com.hayden.commitdiffmodel.entity.CommitDiffId;
import com.hayden.commitdiffmodel.entity.GitDiffs;
import com.hayden.commitdiffmodel.git.GitErrors;
import com.hayden.commitdiffmodel.git.GitFactory;
import com.hayden.commitdiffmodel.git.RepoOperations;
import com.hayden.commitdiffmodel.git.RepositoryHolder;
import com.hayden.commitdiffmodel.git_factory.DiffFactory;
import com.hayden.commitdiffmodel.model.GitRefModel;
import com.hayden.commitdiffmodel.validation.entity.CommitDiffContextCommitVersion;
import com.hayden.commitdiffmodel.validation.repo.CommitDiffContextVersionRepo;
import com.hayden.proto.prototyped.datasources.ai.modelserver.client.ModelServerValidationAiClient;
import com.hayden.proto.prototyped.datasources.ai.modelserver.request.ModelServerChatRequest;
import com.hayden.proto.prototyped.datasources.ai.modelserver.request.RetryParameters;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.next_commit.NextCommitAssert;
import com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op.RepoOpAssertCtx;
import com.hayden.test_graph.commit_diff_context.config.CommitDiffContextConfigProps;
import com.hayden.test_graph.commit_diff_context.config.DbDataSourceTrigger;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.steps.RegisterAssertStep;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.git.RepoUtil;
import com.hayden.utilitymodule.io.FileUtils;
import com.hayden.utilitymodule.result.Result;
import com.hayden.utilitymodule.result.error.SingleError;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Files;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
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
    ParseDiff parseDiff;
    @Autowired
    CommitDiffContextMapper mapper;
    @Autowired
    ModelServerValidationAiClient validationAiClient;
    @Autowired
    CommitDiffContextConfigProps props;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    CommitDiffContextVersionRepo versionRepo;
    @Autowired
    GitFactory gitFactory;
    @Autowired
    DbDataSourceTrigger dbDataSourceTrigger;

    @Given("a postgres database to be loaded from {string} for docker-compose {string}")
    public void startPostgresDatabase(String postgresSource, String dockerCompose) {
        var postgresSrcDir = Paths.get(postgresSource).toFile();
        assertions.assertSoftly(postgresSrcDir.exists() || postgresSrcDir.mkdirs(),
                "Postgres directory %s exists or was able to be created.".formatted(postgresSource));

        final Path dockerComposeFile = Paths.get(dockerCompose, "docker-compose.yml");
        var written = FileUtils.readToString(Paths.get(dockerCompose, "docker-compose-template.yml").toFile())
                .mapError(se -> new FileUtils.FileError(se.getMessage()))
                .flatMapResult(found -> {
                    var replaced = found.replaceAll("\\{\\{postgres_source}}", postgresSource);
                    return FileUtils.writeToFileRes(replaced, dockerComposeFile);
                })
                .one();

        assertions.assertSoftly(written.isOk(), "Was not successfuly generating docker-compose with err: %s"
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

    /**
     * Add git diffs to memory for current most recent commit, then reset to previous commit to prepare for prediction of that commit being removed.
     */
    @And("the most recent commit is saved to memory and removed from the repository")
    public void addMostRecentCommitInfo() {
        var temp = Files.newTemporaryFolder();

        var repoData = repoOpInit.repoDataOrThrow();

        repoOpInit.setRepoData(repoData.withClonedUri(temp.toPath()));

        try (var g = Git.cloneRepository().setBranch(repoData.branchName())
                .setURI(repoData.url())
                .setDirectory(repoData.clonedUri().toFile())
                .setFs(FS.detect())
                .call();
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

                repoOpInit.getLlmValidationData().swap(new RepoOpInit.LlmValidationCommitData(lst.results(), lc));

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


    }

    @And("the AI generated response is compared to the actual commit by calling the validation endpoint {string}")
    public void theAIGeneratedResponseIsComparedToTheActualCommitByCallingTheValidationEndpoint(String endpoint) {
        this.nextCommit.getNextCommitInfo().optional()
                .map(NextCommitAssert.NextCommitMetadata::nc)
                .flatMap(nc -> repoOpInit.getLlmValidationData().optional()
                        .map(llm -> Map.entry(nc, llm)))
                .ifPresent(ncLlm -> {
                    var nc = ncLlm.getKey();
                    var llm = ncLlm.getValue();

                    var inputs = Result.from(
                                    nc.getDiffs().stream().map(mapper::toDiffInput)
                                            .map(DiffFactory::extractApplications))
                            .toList();

                    var allErr = GitErrors.GitAggregateError.from(inputs.errsList());

                    assertions.assertSoftly(inputs.errsList().isEmpty(), "There existed some error: %s".formatted(allErr.getMessage()));

                    var actualCommit = llm.diffs()
                            .stream()
                            .map(ParseDiff.GitDiffResult::diffs)
                            .map(GitDiffs::new)
                            .toList();
                    var llmProvidedCommit = inputs.results().stream()
                            .map(GitDiffs::new)
                            .toList();

                    nextCommit.getActualCommitInfo().swap(CommitDiffContextCommitVersion.AssertedGitDiffs.builder()
                            .actual(actualCommit).nextCommit(llmProvidedCommit).build());

                    try {
                        var actual = objectMapper.writeValueAsString(actualCommit);
                        var llmProvided = objectMapper.writeValueAsString(llmProvidedCommit);
                        var sent = validationAiClient.send(
                                        ModelServerChatRequest.builder()
                                                .content(new ModelServerChatRequest.ModelServerBody("""
                                                        Please rate the following commit by comparing it to the actual commit. Please give it only a rating out of 100 and nothing else.
                                                        Actual:
                                                        %s
                                                        Generated:
                                                        %s
                                                        """
                                                        .formatted(actual, llmProvided),
                                                        ModelServerChatRequest.ModelServerRequestType.VALIDATION))
                                                .path(endpoint)
                                                .url(props.getModelServerBaseUrl())
                                                .retryParameters(new RetryParameters(0))
                                                .headers(new HttpHeaders())
                                                .build())
                                .one();

                        assertions.assertSoftly(sent.isOk(), "Validation was not received from server: %s."
                                .formatted(sent.errorMessage()));

                        sent.ifPresent(mrc -> nextCommit.getValidationResponse().swap(new NextCommitAssert.NextCommitLlmValidation(mrc)));
                    } catch (JsonProcessingException e) {
                        assertions.assertSoftly(false, "Could not serialize response to %s".formatted(llmProvidedCommit));
                    }
                });
    }

    @Then("the validation data is saved for review")
    @RegisterAssertStep({RepoOpAssertCtx.class, NextCommitAssert.class})
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
                        .map(nmm -> Map.entry(nmm, vrc)))
                .ifPresentOrElse(nextCommitMrc -> {
                            var asserted = nextCommitMrc.getKey();
                            var mrc = nextCommitMrc.getValue();
                            this.dbDataSourceTrigger.doWithKey(setKey -> {
                                setKey.setInitialized();
                                var saved = doSaveVersionRepo(mrc, asserted);
                                setKey.setInit();
                                var savedValidation = doSaveVersionRepo(mrc, asserted);
                                setKey.setInitialized();
                            });
                        },
                        () -> assertions.assertSoftly(false, "Validation was not received from server."));
    }

    private @NotNull CommitDiffContextCommitVersion doSaveVersionRepo(Map.Entry<ModelServerValidationAiClient.ValidationResult, RepoOpInit.RepositoryData> mrc, CommitDiffContextCommitVersion.AssertedGitDiffs asserted) {
        var saved = versionRepo.save(CommitDiffContextCommitVersion.builder()
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
}
