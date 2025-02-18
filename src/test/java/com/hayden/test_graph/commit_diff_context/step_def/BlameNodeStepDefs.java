package com.hayden.test_graph.commit_diff_context.step_def;

import com.google.common.collect.Sets;
import com.hayden.commitdiffmodel.entity.CommitDiffContextBlameTree;
import com.hayden.commitdiffmodel.entity.CommitDiffId;
import com.hayden.commitdiffmodel.repo.BlameTreeRepository;
import com.hayden.commitdiffmodel.repo.CommitDiffRepository;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.codegen.Codegen;
import com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op.RepoOpAssertCtx;
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

import java.util.Objects;
import java.util.stream.Collectors;

public class BlameNodeStepDefs implements ResettableStep {

    @Autowired
    @ResettableThread
    private RepoOpInit commitDiffInit;
    @Autowired
    @ResettableThread
    private Assertions assertions;
    @Autowired
    private BlameTreeRepository blameTreeRepository;
    @Autowired
    private CommitDiffRepository commitDiffRepository;
;

    @And("add blame nodes is called")
    @RegisterInitStep(RepoOpInit.class)
    public void add_commit_diff_context_blame_node() {
        commitDiffInit.getRepoInitializations().initItems().add(new RepoOpInit.RepoInitItem.AddBlameNodes());
    }

    @Then("the blame node embeddings are validated to be added to the database")
    @AssertStep(RepoOpAssertCtx.class)
    public void initial_commit_diff_context_blame_node() {
        var blameTrees = blameTreeRepository.findAll();
        var allCommitDiffs = commitDiffRepository.findAll();

        assertions.assertSoftly(!blameTrees.isEmpty(), "Could not find blame tree.");
        assertions.assertSoftly(!allCommitDiffs.isEmpty(), "Could not find commit diffs.");

        var bt = blameTrees.stream().map(CommitDiffContextBlameTree::getParent)
                .map(com.hayden.commitdiffmodel.entity.CommitDiff::getId)
                .map(CommitDiffId::getParentHash)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var cd = allCommitDiffs.stream()
                .map(com.hayden.commitdiffmodel.entity.CommitDiff::getId)
                .map(CommitDiffId::getParentHash)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var int1 = Sets.intersection(bt, cd);
        var int2 = Sets.intersection(bt, cd);

        assertions.assertSoftly(int1.equals(int2), "All commit diffs represented in blame tree.");
        assertions.assertSoftly(bt.size() == cd.size(), "All commit diffs represented in blame tree.");
    }

}
