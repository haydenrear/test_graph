package com.hayden.test_graph.commit_diff_context.step_def;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hayden.commitdiffmodel.codegen.types.CodeQuery;
import com.hayden.commitdiffmodel.codegen.types.CommitMessage;
import com.hayden.commitdiffmodel.codegen.types.EmbeddingIn;
import com.hayden.commitdiffmodel.config.CommitDiffContextProperties;
import com.hayden.commitdiffmodel.entity.CodeBranch;
import com.hayden.commitdiffmodel.entity.Embedding;
import com.hayden.commitdiffmodel.repo.CodeBranchRepository;
import com.hayden.commitdiffmodel.repo.CommitDiffRepository;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op.RepoOpAssertCtx;
import com.hayden.test_graph.commit_diff_context.init.mountebank.ctx.CdMbInitCtx;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoInitItem;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.steps.ExecAssertStep;
import com.hayden.test_graph.steps.RegisterInitStep;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.result.error.SingleError;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;


@Slf4j
public class CodeContextStepDefs implements ResettableStep {

    @Autowired
    @ResettableThread
    RepoOpInit repoOpInit;

    @Autowired
    @ResettableThread
    Assertions assertions;

    @Autowired
    ObjectMapper mapper;
    @Autowired
    CommitDiff commitDiff;

    @Then("retrieve code context data from the server with code query {string}")
    @ExecAssertStep(RepoOpAssertCtx.class)
    public void retrieveCodeContextDataFromTheServerWithQuery(String query) {
        var gitRepoPromptingRequest = repoOpInit.toCommitRequestArgs()
                .commitDiffContextValue();
        gitRepoPromptingRequest
                .nextCommitRequest()
                .setCodeQuery(CodeQuery.newBuilder().codeString(query).build());
        doCallAssertCommitContext();
    }

    @Then("retrieve code context data from the server with code query as commit message {string}")
    @ExecAssertStep(RepoOpAssertCtx.class)
    public void retrieveCodeContextDataFromTheServerWithCodeQueryAsCommitMessage(String arg0) {
        var gitRepoPromptingRequest = repoOpInit.toCommitRequestArgs()
                .commitDiffContextValue();
        gitRepoPromptingRequest
                .nextCommitRequest()
                .setCommitMessage(CommitMessage.newBuilder().value(arg0).build());
        doCallAssertCommitContext();
    }

    @Then("retrieve code context data from the server with code query as embedding loaded from {string}")
    @ExecAssertStep(RepoOpAssertCtx.class)
    public void retrieveCodeContextDataFromTheServerWithCodeQueryAsEmbeddingLoadedFrom(String arg0) {
        try {
            var res = new PathMatchingResourcePatternResolver().getResource(arg0);
            assertions.assertStrongly(res.exists(), "Embedding file does not exist.");
            if (res.exists()) {
                var embedIn = mapper.readValue(res.getFile(), EmbeddingIn.class);
                var gitRepoPromptingRequest = repoOpInit.toCommitRequestArgs()
                        .commitDiffContextValue();
                gitRepoPromptingRequest
                        .nextCommitRequest()
                        .setCodeQuery(CodeQuery.newBuilder().codeEmbedding(embedIn).build());
                doCallAssertCommitContext();
            } else {
                // do something
                log.info("Commit message file {} does not exist.", arg0);
            }
        } catch (IOException e) {
            assertions.assertStrongly(false, "Could not parse commit message: " + arg0);
        }
    }

    public void doCallAssertCommitContext() {
        var res = this.commitDiff.callGraphQlQuery(repoOpInit.toCodeContextRequestArgs());
        assertions.assertSoftly(res.isOk(), "Failed to retrieve code context: %s.".formatted(res.errorMessage()));
    }
}
