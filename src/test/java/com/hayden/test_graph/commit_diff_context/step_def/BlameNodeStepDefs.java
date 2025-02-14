package com.hayden.test_graph.commit_diff_context.step_def;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.codegen.Codegen;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.commit_diff_context.init.mountebank.CdMbInitBubbleCtx;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.steps.AssertStep;
import com.hayden.test_graph.steps.RegisterInitStep;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

public class BlameNodeStepDefs implements ResettableStep {

    @Autowired
    @ResettableThread
    RepoOpInit commitDiffInit;

    @Autowired
    CommitDiff commitDiff;
    @Autowired
    @ResettableThread
    Codegen codegen;
    @Autowired
    @ResettableThread
    Assertions assertions;
    @Autowired
    @ResettableThread
    RepoOpInit bubbleCtx;

    @And("the user requests to get the next commit with commit message {string}")
    public void do_set_user_repo_data(String commitMessage) {
        commitDiffInit.userCodeData().swap(
                RepoOpInit.UserCodeData.builder()
                        .commitMessage(commitMessage)
                        .build());
    }

    @Then("the initial embedding is added for commit diff context blame node")
    @RegisterInitStep(RepoOpInit.class) // TODO: can probably be replaced by first Then - annotating all Then then
    public void initial_commit_diff_context_blame_node() {
    }

    @When("the embeddings are added to the database for the repo by calling commit diff context")
    public void add_embeddings_to_database_for_commit_diff_context() {
    }

    @When("the user requests to get the next commit")
    public void user_requests_next_commit() {
        commitDiff.callGraphQlQuery(commitDiffInit.toCommitRequestArgs());
    }

    @Then("the model responds with valid commit that is committed to the repository successfully")
    @AssertStep(Codegen.class)
    public void assert_model_response() {
        // validate that the embedding exists in the context for any assertion
        assertions.assertThat(codegen.repoUrl().res().one().isPresent()).isTrue();
        assertions.assertThat(codegen.getUserCode().res().one().isPresent()).isTrue();
    }

    @And("the initial code response is loaded from {string}")
    public void initial_code_response(String initialCodeResponseFile) {
    }

    @And("the embeddings response for the initial code response is loaded from {string}")
    public void initial_code_embeddings_response(String embeddingsResponseFile) {
    }

    @And("the embeddings responses for the branch are loaded from {string}")
    public void load_embeddings_response_for_branch(String branchEmbeddingsResponse) {
    }

    @And("the AI code tree response is loaded from {string}")
    public void load_ai_code_tree_response(String aiCodeTreeResponse) {
    }
}
