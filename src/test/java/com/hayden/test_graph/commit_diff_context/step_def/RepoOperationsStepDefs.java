package com.hayden.test_graph.commit_diff_context.step_def;

import com.hayden.commitdiffmodel.entity.CodeBranch;
import com.hayden.commitdiffmodel.repo.CodeBranchRepository;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op.RepoOpAssertCtx;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.commit_diff_context.init.mountebank.CdMbInitCtx;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.steps.AssertStep;
import com.hayden.test_graph.steps.RegisterInitStep;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

public class RepoOperationsStepDefs implements ResettableStep {

    @Autowired
    @ResettableThread
    RepoOpInit commitDiffInit;

    @Autowired
    @ResettableThread
    CommitDiff commitDiff;

    @Autowired
    @ResettableThread
    RepoOpAssertCtx commitDiffAssert;
    @Autowired
    CdMbInitCtx ctx;

    @Autowired
    @ResettableThread
    Assertions assertions;

    @Autowired
    private CodeBranchRepository codeBranchRepository;

    @And("there is a repository at the url {string}")
    public void do_set_repo_given(String repoUrl) {
        commitDiffInit.repoData().set(
                RepoOpInit.RepositoryData.builder()
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
    @RegisterInitStep(CdMbInitCtx.class)
    public void addResponseType(String responseType, String fileLocation, String uri, String port) {
        ctx.addAiServerResponse(new CdMbInitCtx.AiServerResponse.FileSourceResponse(
                Paths.get(fileLocation),
                CdMbInitCtx.AiServerResponse.AiServerResponseType.valueOf(responseType),
                new CdMbInitCtx.ModelServerRequestData(uri, 200, Integer.parseInt(port))));
    }

    @And("the add repo GraphQl query {string}")
    @RegisterInitStep(CommitDiffInit.class)
    public void do_set_graph_ql_add_repo(String repoUrl) {
        var repoFile = new File(repoUrl);
        if (!repoFile.exists())
            assertions.assertStrongly(false, "Repository did not exist.");

        commitDiffInit.graphQlQueries().set(
                RepoOpInit.GraphQlQueries.builder().addRepo(repoFile).build());
    }

    @And("a branch should be added {string}")
    public void do_set_branch_to_add(String arg0) {
        var r = commitDiffInit.repoDataOrThrow();
        RepoOpInit.RepositoryData repoData = RepoOpInit.RepositoryData.builder()
                .url(r.url())
                .branchName(arg0)
                .build();
        commitDiffInit.repoData().set(repoData);
        commitDiffInit.getRepoInitializations()
                .initItems()
                .add(new RepoOpInit.RepoInitItem.AddCodeBranch(repoData));
    }

    @When("the repo is added to the database by calling commit diff context")
    @RegisterInitStep(CdMbInitCtx.class)
    public void add_repo_to_database() {
    }

    @And("the embeddings for the branch should be added")
    public void addEmbeddings() {
        commitDiffInit.getRepoInitializations().initItems().add(new RepoOpInit.RepoInitItem.AddEmbeddings());
    }

    @Then("a branch with name {string} will be added to the database")
    @AssertStep(RepoOpAssertCtx.class)
    public void validate_branch_added(String branchAdded) {
        commitDiffAssert.getRepositoryAssertionDescriptor()
                .set(new RepoOpAssertCtx.RepoOpAssertionDescriptor(branchAdded));
        assertions.assertStrongly(commitDiffAssert.isValidParent(),
                "Failed to set validated on parent.");
    }

    @Then("the branch will be added to the database")
    public void theBranchWillBeAddedToTheDatabase() {
        var found = codeBranchExists();
        found.ifPresent(cb -> {
            // verify code branch
        });
    }

    @Then("the branches embeddings will be added to the database")
    public void theBranchesEmbeddingsWillBeAddedToTheDatabase() {
        var found = codeBranchExists();
        found.ifPresent(cb -> {
            // verify code branch embeddings
        });
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
