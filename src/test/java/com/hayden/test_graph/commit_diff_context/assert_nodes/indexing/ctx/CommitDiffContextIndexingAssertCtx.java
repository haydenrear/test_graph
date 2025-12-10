package com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.ctx;

import com.hayden.commitdiffcontext.code_search.libs.res.Dependency;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.assert_g.ctx.AssertCtx;
import com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.CommitDiffContextIndexingAssertNode;
import com.hayden.test_graph.commit_diff_context.data_dep.indexing.ctx.CommitDiffContextIndexingDataDepCtx;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ResettableThread
@RequiredArgsConstructor
@Profile("indexing")
public class CommitDiffContextIndexingAssertCtx implements AssertCtx {

    @Builder
    public record IndexingResult(
            String repoUrl,
            String branch,
            long indexedFileCount,
            long symbolCount,
            boolean indexingSuccessful,
            String statusMessage
    ) {}

    private final ContextValue<CommitDiffContextIndexingAssertBubble> bubbleUnderlying;

    private final ContextValue<CommitDiffContextIndexingDataDepCtx> dataDepContext;

    @Getter
    private final List<IndexingResult> results = new ArrayList<>();

    @Getter
    private final ContextValue<Boolean> validationPassed;

    @Getter
    private final ContextValue<Dependency> dep;

    @Autowired
    private Assertions assertions;

    public List<IndexingResult> results() {
        return results;
    }

    public CommitDiffContextIndexingAssertCtx() {
        this(ContextValue.empty(), ContextValue.empty(), ContextValue.empty(), ContextValue.empty());
    }

    @Autowired
    public void setBubble(CommitDiffContextIndexingAssertBubble bubble) {
        this.bubbleUnderlying.swap(bubble);
        this.bubbleUnderlying.res().one().get().getIndexingAssertCtx().swap(this);
    }

    public void setDependency(Dependency dep) {
        this.dep.swap(dep);
    }

    public void setDataDepContext(CommitDiffContextIndexingDataDepCtx ctx) {
        this.dataDepContext.swap(ctx);
    }

    public ContextValue.MutableContextValue<CommitDiffContextIndexingDataDepCtx, ContextValue.ContextValueError> getDataDepContext() {
        return this.dataDepContext.res();
    }

    public void addResult(IndexingResult result) {
        this.results.add(result);
    }

    public void markValidationPassed() {
        this.validationPassed.swap(true);
    }

    public void markValidationFailed() {
        this.validationPassed.swap(false);
    }

    public void assertIndexingSuccessful() {
        assertions.assertSoftly(!results.isEmpty(), "No indexing results to validate");
        results.forEach(result -> {
            assertions.assertSoftly(result.indexingSuccessful(),
                    "Indexing failed for repo: " + result.repoUrl() + ", status: " + result.statusMessage());
            assertions.assertSoftly(result.indexedFileCount() > 0,
                    "No files were indexed for repo: " + result.repoUrl());
        });
    }

    public void assertSymbolsIndexed() {
        assertions.assertSoftly(!results.isEmpty(), "No indexing results to validate");
        results.forEach(result -> {
            assertions.assertSoftly(result.symbolCount() > 0,
                    "No symbols were indexed for repo: " + result.repoUrl());
        });
    }

    /**
     * Gets the MinIO configuration if it's enabled.
     */
    public boolean isMinIOEnabled() {
        return dataDepContext.optional()
                .map(CommitDiffContextIndexingDataDepCtx::isMinIOEnabled)
                .orElse(false);
    }

    public boolean isClusterEnabled() {
        return isMinIOEnabled() || isKafkaEnabled() || isPersisterEnabled();
    }

    /**
     * Gets the Kafka configuration if it's enabled.
     */
    public boolean isKafkaEnabled() {
        return dataDepContext.optional()
                .map(CommitDiffContextIndexingDataDepCtx::isKafkaEnabled)
                .orElse(false);
    }

    /**
     * Gets the persister configuration if it's enabled.
     */
    public boolean isPersisterEnabled() {
        return dataDepContext.optional()
                .map(CommitDiffContextIndexingDataDepCtx::isPersisterEnabled)
                .orElse(false);
    }

    /**
     * Gets the kubeconfig path from the data dependency context.
     */
    public String getKubeConfigPath() {
        return dataDepContext.optional()
                .flatMap(CommitDiffContextIndexingDataDepCtx::getKubeConfigPath)
                .map(java.nio.file.Path::toString)
                .orElse(null);
    }

    /**
     * Gets the MinIO configuration from the data dependency context.
     */
    public CommitDiffContextIndexingDataDepCtx.ServiceConfig getMinioConfig() {
        return dataDepContext.optional()
                .flatMap(CommitDiffContextIndexingDataDepCtx::getMinioConfig)
                .orElse(null);
    }

    /**
     * Gets the Kafka configuration from the data dependency context.
     */
    public CommitDiffContextIndexingDataDepCtx.ServiceConfig getKafkaConfig() {
        return dataDepContext.optional()
                .flatMap(CommitDiffContextIndexingDataDepCtx::getKafkaConfig)
                .orElse(null);
    }

    /**
     * Gets the persister configuration from the data dependency context.
     */
    public CommitDiffContextIndexingDataDepCtx.ServiceConfig getPersisterConfig() {
        return dataDepContext.optional()
                .flatMap(CommitDiffContextIndexingDataDepCtx::getPersisterConfig)
                .orElse(null);
    }

    @Override
    public CommitDiffContextIndexingAssertBubble bubble() {
        return this.bubbleUnderlying.res().one().get();
    }

    @Override
    public Class<CommitDiffContextIndexingAssertBubble> bubbleClazz() {
        return CommitDiffContextIndexingAssertBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffContextIndexingAssertNode;
    }

    public ContextValue<CommitDiffContextIndexingDataDepCtx> dataDepContext() {
        return dataDepContext;
    }
}
