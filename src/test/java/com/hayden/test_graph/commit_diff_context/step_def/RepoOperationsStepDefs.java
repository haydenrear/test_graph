package com.hayden.test_graph.commit_diff_context.step_def;

import com.hayden.commitdiffmodel.entity.CodeBranch;
import com.hayden.commitdiffmodel.entity.Embedding;
import com.hayden.commitdiffmodel.git.RepositoryHolder;
import com.hayden.commitdiffmodel.repo.CodeBranchRepository;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op.RepoOpAssertCtx;
import com.hayden.test_graph.commit_diff_context.init.mountebank.ctx.CdMbInitCtx;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoInitItem;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.steps.ExecAssertStep;
import com.hayden.test_graph.steps.RegisterInitStep;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.db.DbDataSourceTrigger;
import com.hayden.utilitymodule.result.error.SingleError;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;


public class RepoOperationsStepDefs implements ResettableStep {

    @Autowired
    @ResettableThread
    RepoOpInit commitDiffInit;

    @Autowired
    @ResettableThread
    RepoOpAssertCtx commitDiffAssert;

    @Autowired
    @ResettableThread
    Assertions assertions;

    @Autowired
    @ResettableThread
    CdMbInitCtx ctx;

    @Autowired
    CodeBranchRepository codeBranchRepository;
    @Autowired
    PathMatchingResourcePatternResolver resolver;
    @Autowired
    DbDataSourceTrigger trigger;

    @Autowired
    @ResettableThread
    public void setCommitDiffInit(RepoOpInit commitDiffInit) {
        this.commitDiffInit = commitDiffInit;
    }

    @Autowired
    @ResettableThread
    DockerInitCtx dockerInitCtx;

    @And("there is a repository at the url {string}")
    @RegisterInitStep(RepoOpInit.class)
    public void do_set_repo_given(String repoUrl) {
        dockerInitCtx.getStarted().swap(true);
        trigger.doWithKey(setKey -> {
            var currKey = setKey.curr();
            var p = codeBranchRepository.withCommitsWithDiffs(RepositoryHolder.RepositoryArgs.builder()
                    .branch("main")
                    .repoPath("/Users/hayde/IdeaProjects/test_graph_next")
                    .build());
            System.out.println();
        }) ;
        commitDiffInit.setRepoData(
                RepoOpInit.RepositoryData.builder()
                        .clonedUri(Paths.get(repoUrl))
                        .url(repoUrl)
                        .build());
    }

    /**
     * Ok to call multiple times - will just register additional inforation
     * @param responseType
     * @param fileLocation
     * @param uri
     * @param port
     */
    @And("There exists a response type of {string} in the file location {string} for model server endpoint {string} on port {string}")
    @RegisterInitStep(RepoOpInit.class)
    public void addResponseType(String responseType, String fileLocation, String uri, String port) {
        registerResponse(responseType, fileLocation, uri, port, "-1");
    }

    @And("There exists a response type of {string} in the file location {string} for model server endpoint {string} on port {string} for the {string} response")
    @RegisterInitStep(RepoOpInit.class)
    public void addResponseTypeWithCount(String responseType, String fileLocation, String uri, String port, String count) {
        registerResponse(responseType, fileLocation, uri, port, count);
    }

    @And("There exists an inject response type of {string} in the file location {string} for model server endpoint {string} on port {string}")
    public void registerInjectResponse(String responseType, String fileLocation, String uri, String port) {
        registerInjectResponse(responseType, fileLocation, uri, port, "-1");
    }

    @And("the add repo GraphQl query {string}")
    @RegisterInitStep(RepoOpInit.class)
    public void do_set_graph_ql_add_repo(String repoUrl) {
        var repoFile = new File(repoUrl);
        if (!repoFile.exists())
            assertions.assertStrongly(false, "Repository did not exist.");

        commitDiffInit.graphQlQueries().swap(
                RepoOpInit.GraphQlQueries.builder().addRepo(repoFile).build());
    }

    @And("a branch should be added {string}")
    @RegisterInitStep(RepoOpInit.class)
    public void do_set_branch_to_add(String arg0) {
        var r = commitDiffInit.repoDataOrThrow();
        RepoOpInit.RepositoryData repoData = r.withBranch(arg0);
        commitDiffInit.setRepoData(repoData);
        commitDiffInit.getRepoInitializations()
                .initItems()
                .add(new RepoInitItem.AddCodeBranch(repoData));
    }

    @When("the repo is added to the database by calling commit diff context")
    @RegisterInitStep(RepoOpInit.class)
    public void add_repo_to_database() {
    }

    @And("the embeddings for the branch should be added")
    public void addEmbeddings() {
        commitDiffInit.getRepoInitializations().initItems().add(new RepoInitItem.AddEmbeddings());
    }

    @Then("a branch with name {string} will be added to the database")
    @ExecAssertStep(RepoOpAssertCtx.class)
    public void validate_branch_added(String branchAdded) {
        commitDiffAssert.getRepositoryAssertionDescriptor()
                .swap(new RepoOpAssertCtx.RepoOpAssertionDescriptor(branchAdded));
        assertions.assertStrongly(commitDiffAssert.isValidParent(),
                "Failed to set validated on parent.");
    }

    @Then("the branch will be added to the database")
    public void theBranchWillBeAddedToTheDatabase() {
        assertions.assertSoftly(commitDiffInit.repoDataOrNull() != null, "Repo data was null.");
        var found = codeBranchExists();
        assertions.assertSoftly(found.isPresent(), "Code Branch for %s does not exist.".formatted(commitDiffInit.repoDataOrNull()));
    }

    @Then("the branches embeddings will be added to the database")
    @ExecAssertStep(RepoOpAssertCtx.class)
    public void theBranchesEmbeddingsWillBeAddedToTheDatabase() {
        var repoData = commitDiffInit.repoDataOrThrow();
        var found = codeBranchRepository.withCommitsWithDiffs(repoData.toRepositoryArgs());

        assertions.assertSoftly(found.isPresent(), "Code branch did not exist, %s."
                .formatted(repoData.toRepositoryArgs()));
        found.ifPresent(cb -> {
            var commitDiffs = cb.parseCommitDiffs();
            assertions.assertSoftly(commitDiffs.isOk(), "Commit diffs could not be retrieved from %s with err: %s."
                    .formatted(cb.getBranchName(), commitDiffs.errorMessage()));

            commitDiffs.one().ifPresent(cd -> {
                for (var cdFound : cd)
                    assertions.assertSoftly(!Arrays.equals(cdFound.embedding(), Embedding.INITIALIZED),
                            "Commit diff %s was not initialized.".formatted(cdFound.getId()));
            });
        });
    }

    private void registerInjectResponse(String responseType, String fileLocation, String uri, String port, String count) {
        var responseFile = resolver.getResource(fileLocation);
        assertions.assertStrongly(responseFile.exists(), "Response did exist.");
        try {
            ctx.addAiServerResponse(new CdMbInitCtx.AiServerResponse.FileSourceFunctionResponse(
                            responseFile.getFile().toPath(),
                            CdMbInitCtx.AiServerResponse.AiServerResponseType.valueOf(responseType),
                            new CdMbInitCtx.ModelServerRequestData(uri, 200, Integer.parseInt(port))),
                    Integer.parseInt(count));
        } catch (IOException e) {
            assertions.assertStronglyPattern(false, "Failed to add response for %s: %s\n%s.",
                    responseType, e.getMessage(), SingleError.parseStackTraceToString(e));
        }
    }

    private void registerResponse(String responseType, String fileLocation, String uri, String port, String count) {
        var responseFile = resolver.getResource(fileLocation);
        assertions.assertStrongly(responseFile.exists(), "Response did exist.");
        try {
            ctx.addAiServerResponse(new CdMbInitCtx.AiServerResponse.FileSourceResponse(
                            responseFile.getFile().toPath(),
                            CdMbInitCtx.AiServerResponse.AiServerResponseType.valueOf(responseType),
                            new CdMbInitCtx.ModelServerRequestData(uri, 200, Integer.parseInt(port))),
                    Integer.parseInt(count));
        } catch (IOException e) {
            assertions.assertStronglyPattern(false, "Failed to add response for %s: %s\n%s.",
                    responseType, e.getMessage(), SingleError.parseStackTraceToString(e));
        }
    }

    private @NotNull Optional<CodeBranch> codeBranchExists() {
        var repoData = commitDiffInit.repoDataOrThrow();
        var found = codeBranchRepository.findByBranchNameWithParent(repoData.branchName(),
                repoData.url());
        assertions.assertSoftly(found.isPresent(), "Code branch did not exist",
                "Code branch existed.");
        return found;
    }

}
