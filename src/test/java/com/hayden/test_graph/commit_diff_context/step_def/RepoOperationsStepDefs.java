package com.hayden.test_graph.commit_diff_context.step_def;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op.RepoOpCtx;
import com.hayden.test_graph.commit_diff_context.init.ctx.CommitDiffInit;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.steps.AssertStep;
import com.hayden.test_graph.steps.InitStep;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class RepoOperationsStepDefs implements ResettableStep {

    @Autowired
    @ResettableThread
    CommitDiffInit commitDiffInit;

    @Autowired
    @ResettableThread
    CommitDiff commitDiff;

    @Autowired
    @ResettableThread
    RepoOpCtx commitDiffAssert;

    @Autowired
    @ResettableThread
    Assertions assertions;

    @And("there is a repository at the url {string}")
    public void do_set_repo_given(String repoUrl) {
        commitDiffInit.repoData().set(
                CommitDiffInit.RepositoryData.builder()
                        .url(repoUrl)
                        .build());
    }

    @And("the add repo GraphQl query {string}")
    @InitStep(CommitDiffInit.class)
    public void do_set_graph_ql_add_repo(String repoUrl) {
        var repoFile = new File(repoUrl);
        if (!repoFile.exists())
            assertions.assertStrongly(false, "Repository did not exist.");

        commitDiffInit.graphQlQueries().set(
                CommitDiffInit.GraphQlQueries.builder().addRepo(repoFile).build());
    }

    @And("a branch should be added {string}")
    public void do_set_branch_to_add(String arg0) {
        var r = commitDiffInit.repoDataOrThrow();
        commitDiffInit.repoData().set(
                CommitDiffInit.RepositoryData.builder()
                        .url(r.url())
                        .branchName(arg0)
                        .build());
    }

    @When("the repo is added to the database by calling commit diff context")
    @InitStep(CommitDiffInit.class)
    public void add_repo_to_database() {
    }

    @Then("a branch with name {string} will be added to the database")
    public void validate_branch_added(String branchAdded) {
        commitDiffAssert.getRepositoryAssertionDescriptor()
                .set(new RepoOpCtx.RepoOpAssertionDescriptor(branchAdded));
    }

    @And("all repository operations are validated")
    @AssertStep(RepoOpCtx.class)
    public void validate_repo_operations() {
    }
}
