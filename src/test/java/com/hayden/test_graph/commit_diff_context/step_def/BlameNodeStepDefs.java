package com.hayden.test_graph.commit_diff_context.step_def;

import com.hayden.test_graph.commit_diff_context.ctx.CommitDiffAssert;
import com.hayden.test_graph.commit_diff_context.ctx.CommitDiffInit;
import com.hayden.test_graph.commit_diff_context.service.CommitDiffContext;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.steps.AssertStep;
import com.hayden.test_graph.steps.InitStep;
import com.hayden.test_graph.thread.ThreadScope;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class BlameNodeStepDefs {

    @Autowired
    @ThreadScope
    CommitDiffInit commitDiffInit;

    @Autowired
    @ThreadScope
    DockerInitCtx dockerInitCtx;

    @Autowired
    CommitDiffContext commitDiffContext;

    @Given("docker-compose is started from {string}")
    public void docker_compose_started(String composePath) {
        dockerInitCtx.getComposePath().set(new File(composePath));
    }

    @And("there is a repository at the url {string} with branch {string} checked out and next commit message from user {string}")
    @InitStep(CommitDiffInit.class)
    public void do_set_repo_given(String repoUrl, String branch, String commitMessage) {
        commitDiffInit.repoData().set(
                CommitDiffInit.RepositoryData.builder()
                        .url(repoUrl)
                        .branchName(branch)
                        .build()
        );
        commitDiffInit.userCodeData().set(
                CommitDiffInit.UserCodeData.builder()
                        .commitMessage(commitMessage)
                        .build()
        );
    }


    @When("the user requests to get the next commit")
    public void user_requests_next_commit() {
        commitDiffContext.requestCommit(commitDiffInit.toCommitRequestArgs());
    }

    @Then("the model responds with valid commit that is committed to the repository successfully")
    @AssertStep(CommitDiffAssert.class)
    public void assert_model_response() {
    }

}
