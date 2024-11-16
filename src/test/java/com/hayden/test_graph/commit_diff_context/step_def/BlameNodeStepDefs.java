package com.hayden.test_graph.commit_diff_context.step_def;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.CommitDiffAssert;
import com.hayden.test_graph.commit_diff_context.assert_nodes.codegen.Codegen;
import com.hayden.test_graph.commit_diff_context.init.ctx.CommitDiffInit;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.steps.AssertStep;
import com.hayden.test_graph.steps.InitStep;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class BlameNodeStepDefs implements ResettableStep {

    @Autowired
    @ResettableThread
    CommitDiffInit commitDiffInit;

    @Autowired
    @ResettableThread
    DockerInitCtx dockerInitCtx;

    @Autowired
    CommitDiff commitDiff;
    @Autowired
    Codegen codegen;
    @Autowired
    Assertions assertions;

    @And("the user requests to get the next commit with commit message {string}")
    @InitStep(CommitDiffInit.class)
    public void do_set_user_repo_data(String commitMessage) {
        commitDiffInit.userCodeData().set(
                CommitDiffInit.UserCodeData.builder()
                        .commitMessage(commitMessage)
                        .build());
    }

    @When("the user requests to get the next commit")
    public void user_requests_next_commit() {
        commitDiff.requestCommit(commitDiffInit.toCommitRequestArgs());
    }

    @Then("the model responds with valid commit that is committed to the repository successfully")
    @AssertStep(Codegen.class)
    public void assert_model_response() {
        // validate that the data exists in the context for any assertion
        assertions.assertThat(codegen.repoUrl().res().isPresent()).isTrue();
        assertions.assertThat(codegen.getUserCode().res().isPresent()).isTrue();
    }

}
