package com.hayden.test_graph.commit_diff_context.step_def;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hayden.commitdiffmodel.entity.*;
import com.hayden.commitdiffmodel.repo.CommitDiffClusterRepository;
import com.hayden.commitdiffmodel.repo.CommitDiffItemRepository;
import com.hayden.commitdiffmodel.repo.CommitDiffRepository;
import com.hayden.commitdiffmodel.repo.EmbeddedGitDiffRepository;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op.RepoOpAssertCtx;
import com.hayden.commitdiffmodel.config.CommitDiffContextConfigProps;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoInitItem;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.steps.RegisterAssertStep;
import com.hayden.test_graph.steps.RegisterInitStep;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class BlameNodeStepDefs implements ResettableStep {

    @Autowired
    @ResettableThread
    private RepoOpInit commitDiffInit;
    @Autowired
    @ResettableThread
    private Assertions assertions;

    @Autowired
    private CommitDiffRepository commitDiffRepository;
    @Autowired
    private CommitDiffItemRepository itemRepository;
    @Autowired
    private CommitDiffClusterRepository commitDiffClusterRepository;
    @Autowired
    private EmbeddedGitDiffRepository embeddedGitDiffRepository;

    @And("add blame nodes is called")
    @RegisterInitStep(RepoOpInit.class)
    public void add_commit_diff_context_blame_node() {
        commitDiffInit.getRepoInitializations().initItems()
                .add(new RepoInitItem.AddBlameNodes());
    }

    @Then("the blame node embeddings are validated to be added to the database")
    @RegisterAssertStep(RepoOpAssertCtx.class)
    public void initial_commit_diff_context_blame_node() {
        assertBlameTrees();
        assertCommitDiffClusters();
        assertCommitDiffs();
        assertCommitDiffItems();
        assertions.assertSoftly(assertGitDiffEmbeddingItems(), "No git diff embeddings were embedded");
    }

    private void assertCommitDiffItems() {
        AtomicBoolean isEmpty = new AtomicBoolean(true);
        itemRepository.doWithStream(i -> {
            isEmpty.set(false);
            if (!isEmpty.get()) {
                return;
            }
            assertions.assertSoftly(this.assertCommitDiffItem(i), "No commit diff item were embedded");
        });

        assertions.assertSoftly(!isEmpty.get(), "Commit diff items were empty.");
    }

    private boolean assertGitDiffEmbeddingItems() {
        List<EmbeddedGitDiff> all = embeddedGitDiffRepository.findAll();
        assertions.assertSoftly(!all.isEmpty(), "Embedded git diff items were empty.");
        return all.stream().anyMatch(this::isInitializedEmbedding);
    }

    private void assertBlameTrees() {
        var cd = commitDiffRepository.findAll()
                .stream()
                .map(CommitDiff::getId)
                .map(CommitDiffId::getParentHash)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        assertions.assertSoftly(!cd.isEmpty(), "Could not find commit diffs.");

    }

    private void assertCommitDiffClusters() {
        var commitDiffClusters = commitDiffClusterRepository.findAllWithCommitDiffs();
        assertions.assertSoftly(!commitDiffClusters.isEmpty(), "Commit diff clusters were empty.");
        assertions.assertSoftly(commitDiffClusters.stream().anyMatch(this::assertCommitDiffCluster), "No commit diff clusters were embedded");
    }

    private void assertCommitDiffs() {
        List<CommitDiff> commitDiffs = commitDiffRepository.findAllWithCommitDiffItems();
        assertions.assertSoftly(commitDiffs.stream().anyMatch(this::assertCommitDiff), "No commit diffs were embedded.");
        assertions.assertSoftly(!commitDiffs.isEmpty(), "Commit diffs were empty.");
    }

    private boolean assertCommitDiffCluster(CommitDiffCluster cdc) {
        if (cdc.getCommitDiffs().isEmpty())
            return false;
        return isInitializedEmbedding(List.of(cdc));
    }

    private boolean assertCommitDiff(CommitDiff cdi) {
        if (cdi.getDiffs().isEmpty())
            return false;

        return isInitializedEmbedding(List.of(cdi));
    }

    private boolean assertCommitDiffItem(CommitDiffItem cdi) {
        if (cdi.getParsed().isEmpty())
            return false;
        return isInitializedEmbedding(List.of(cdi))
                && isInitializedEmbedding(cdi.getParsed());
    }

    private <T extends SerializableEmbed & HasEmbedding> boolean  isInitializedEmbedding(Collection<T> cdc) {
        return cdc.stream().anyMatch(this::isInitializedEmbedding);
    }

    private <T extends SerializableEmbed & HasEmbedding> boolean isInitializedEmbedding(T embeddedItem) {
        return embeddedItem.isEmbedded();
    }

}
