package com.hayden.test_graph.commit_diff_context.step_def;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.codegen.Codegen;
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
;

    @And("add blame nodes is called")
    @RegisterInitStep(RepoOpInit.class)
    public void add_commit_diff_context_blame_node() {
//        commitDiffInit.getRepoInitializations().initItems().add(new RepoOpInit.RepoInitItem.AddBlameNodes());
        throw new RuntimeException("Not implemented yet. Blame node not saved to DB yet.");
    }

    @Then("the blame node embeddings are validated to be added to the database")
    @RegisterInitStep(RepoOpInit.class)
    public void initial_commit_diff_context_blame_node() {
        throw new RuntimeException("Not implemented yet. Blame node not saved to DB yet.");
    }

}
