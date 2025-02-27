package com.hayden.test_graph.commit_diff_context.step_def;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.hayden.commitdiffmodel.entity.*;
import com.hayden.commitdiffmodel.repo.BlameTreeRepository;
import com.hayden.commitdiffmodel.repo.CommitDiffClusterRepository;
import com.hayden.commitdiffmodel.repo.CommitDiffItemRepository;
import com.hayden.commitdiffmodel.repo.CommitDiffRepository;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op.RepoOpAssertCtx;
import com.hayden.test_graph.commit_diff_context.config.CommitDiffContextConfigProps;
import com.hayden.test_graph.commit_diff_context.init.mountebank.ctx.CdMbInitCtx;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoInitItem;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.steps.RegisterAssertStep;
import com.hayden.test_graph.steps.RegisterInitStep;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import com.querydsl.core.types.OrderSpecifier;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
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
    private BlameTreeRepository blameTreeRepository;
    @Autowired
    private CommitDiffRepository commitDiffRepository;
    @Autowired
    private CommitDiffItemRepository itemRepository;
    @Autowired
    private CommitDiffClusterRepository commitDiffClusterRepository;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private CommitDiffContextConfigProps contextConfigProps;

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
    }

    private void assertCommitDiffItems() {
        AtomicBoolean isEmpty = new AtomicBoolean(true);
        itemRepository.doWithStream(i -> {
            isEmpty.set(false);
            this.assertCommitDiffItem(i);
        });

        assertions.assertSoftly(!isEmpty.get(), "Commit diff items were empty.");
    }

    private void assertBlameTrees() {
        var cd = commitDiffRepository.findAll()
                .stream()
                .map(CommitDiff::getId)
                .map(CommitDiffId::getParentHash)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        assertions.assertSoftly(!cd.isEmpty(), "Could not find commit diffs.");

        var blameTrees = blameTreeRepository.findAll();
        assertions.assertSoftly(!blameTrees.isEmpty(), "Could not find blame tree.");

        var bt = blameTrees.stream().map(CommitDiffContextBlameTree::getParent)
                .map(CommitDiff::getId)
                .map(CommitDiffId::getParentHash)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var int1 = Sets.intersection(bt, cd);
        var int2 = Sets.intersection(bt, cd);

        assertions.assertSoftly(int1.equals(int2), "All commit diffs represented in blame tree.");
        assertions.assertSoftly(bt.size() == cd.size(), "All commit diffs represented in blame tree.");
    }

    private void assertCommitDiffClusters() {
        var commitDiffClusters = commitDiffClusterRepository.findAll();
        assertions.assertSoftly(!commitDiffClusters.isEmpty(), "Commit diff clusters were empty.");
        commitDiffClusters.forEach(this::assertCommitDiffCluster);
    }

    private void assertCommitDiffs() {
        List<CommitDiff> commitDiffs = commitDiffRepository.findAll();
        commitDiffs.forEach(this::assertCommitDiff);
        assertions.assertSoftly(!commitDiffs.isEmpty(), "Commit diffs were empty.");
    }

    private void assertCommitDiffCluster(CommitDiffCluster cdc) {
        isInitializedEmbedding(List.of(cdc),
                "Some commit diffs clusters were not embedded.");
    }

    private void assertCommitDiff(CommitDiff cdi) {
        isInitializedEmbedding(List.of(cdi), "Commit diff was not embedded.");
    }

    private void assertCommitDiffItem(CommitDiffItem cdi) {
        isInitializedEmbedding(List.of(cdi), "Embedded diff diff was not embedded.");
        isInitializedEmbedding(cdi.getParsed(), "Some git diffs were not embedded.");
    }

    private <T extends SerializableEmbed> void  isInitializedEmbedding(Collection<T> cdc, String message) {
        assertions.assertSoftly(cdc.stream().allMatch(embeddedItem -> isInitializedEmbedding(embeddedItem, om)), message);
    }

    private boolean isInitializedEmbedding(SerializableEmbed embeddedItem, ObjectMapper om) {
        var isInitialized = embeddedItem.isEmbedded();
        if (!isInitialized) {
            try {
                if (embeddedItem.serialize(om).length > contextConfigProps.getTestEmbeddingMaxSide()) {
                    log.info("Could not embed {}.", embeddedItem);
                    return true;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return isInitialized;
    }
}
