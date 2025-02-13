package com.hayden.test_graph.commit_diff_context.step_def;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hayden.commitdiffmodel.codegen.types.*;
import com.hayden.commitdiffmodel.convert.CommitDiffContextMapper;
import com.hayden.commitdiffmodel.repo_actions.GitHandlerActions;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.next_commit.NextCommitAssert;
import com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op.RepoOpAssertCtx;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.commit_diff_context.init.mountebank.CdMbInitBubbleCtx;
import com.hayden.test_graph.commit_diff_context.init.mountebank.CdMbInitCtx;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpInit;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.steps.AssertStep;
import com.hayden.test_graph.steps.ExecInitStep;
import com.hayden.test_graph.steps.RegisterInitStep;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class NextCommitStepDefs implements ResettableStep {

    @Autowired
    @ResettableThread
    CdMbInitBubbleCtx commitDiffInit;
    @Autowired
    @ResettableThread
    RepoOpInit repoOpInit;

    @Autowired
    @ResettableThread
    CommitDiff commitDiff;

    @Autowired
    @ResettableThread
    RepoOpAssertCtx commitDiffAssert;
    @Autowired
    @ResettableThread
    CdMbInitCtx ctx;
    @Autowired
    @ResettableThread
    NextCommitAssert nextCommit;

    @Autowired
    @ResettableThread
    Assertions assertions;
    @Autowired
    @ResettableThread
    RepoOpInit bubbleCtx;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    CommitDiffContextMapper commitDiffContextMapper;


    @And("a request for the next commit is provided for the given url and branch name provided")
    @RegisterInitStep(value = {CommitDiffInit.class})
    public void nextCommit() {
        var gitRepoPromptingRequest = repoOpInit.toCommitRequestArgs().commitDiffContextValue();
        var repoRequest = repoOpInit.getCommitDiffContextValue().repositoryRequest();
        repoOpInit.repoData().res()
                .optional()
                .ifPresentOrElse(red -> {
                    gitRepoPromptingRequest.repositoryRequest().getGitRepo().setPath(red.url());
                    gitRepoPromptingRequest.addRepo().setBranchName(red.branchName());
                    repoRequest.getGitRepo().setPath(red.url());
                    repoRequest.getGitBranch().setBranch(red.branchName());
                }, () -> {
                    if (gitRepoPromptingRequest.repositoryRequest().getGitRepo() == null) {
                        assertions.assertStrongly(false, "No branch information provided.");
                    }
                    if (repoRequest.getGitRepo() == null) {
                        assertions.assertStrongly(false, "No branch information provided.");
                    }
                });

    }

    @And("a request for the next commit is provided with the commit message being provided from {string}")
    @RegisterInitStep(value = {CommitDiffInit.class})
    public void setCommitMessageForRequest(String commitMessageJson) {
        try {
            var gitRepoPromptingRequest = repoOpInit.toCommitRequestArgs().commitDiffContextValue();
            var res = new PathMatchingResourcePatternResolver().getResource(commitMessageJson);
            assertions.assertStrongly(res.exists(), "Commit message file does not exist.");
            if (res.exists()) {
                var commitMessage = mapper.readValue(res.getFile(), CommitMessage.class);
                gitRepoPromptingRequest.addRepo()
                        .setCommitMessage(commitMessage);
                repoOpInit.userCodeData().swap(
                        RepoOpInit.UserCodeData.builder()
                                .commitMessage(commitMessage.getValue())
                                .build());
            } else {
                // do something
                log.info("Commit message file {} does not exist.", commitMessageJson);
            }
        } catch (IOException e) {
            assertions.assertStrongly(false, "Could not parse commit message: " + commitMessageJson);
        }
    }

    @And("a request for the next commit is provided with the staged information being provided from {string}")
    @RegisterInitStep(value = {CommitDiffInit.class})
    public void setStagedInformationFromJson(String commitMessageJson) {
        try {
            var staged = mapper.readValue(new File(commitMessageJson), Staged.class);
            var gitRepoPromptingRequest = repoOpInit.toCommitRequestArgs().commitDiffContextValue();
            gitRepoPromptingRequest.addRepo()
                    .setStaged(staged);
        } catch (IOException e) {
            assertions.assertStrongly(false, "Could not parse commit message: " + commitMessageJson);
        }
    }

    @And("a request for the next commit is provided with the contextData being provided from {string}")
    @RegisterInitStep(value = {CommitDiffInit.class})
    public void setContextData(String commitMessageJson) {
        try {
            var staged = mapper.readValue(new File(commitMessageJson), new TypeReference<List<ContextData>>() {});
            var gitRepoPromptingRequest = repoOpInit.toCommitRequestArgs().commitDiffContextValue();
            gitRepoPromptingRequest
                    .addRepo()
                    .setContextData(staged);
        } catch (IOException e) {
            assertions.assertStrongly(false, "Could not parse commit message: " + commitMessageJson);
        }

    }

    @And("a request for the next commit is provided with the previous requests being provided from {string}")
    @RegisterInitStep(value = {CommitDiffInit.class})
    public void setPreviousRequests(String commitMessageJson) {
        try {
            var staged = mapper.readValue(new File(commitMessageJson), PrevCommit.class);
            var gitRepoPromptingRequest = repoOpInit.toCommitRequestArgs().commitDiffContextValue();
            gitRepoPromptingRequest
                    .addRepo()
                    .setPrev(staged);
        } catch (IOException e) {
            assertions.assertStrongly(false, "Could not parse commit message: " + commitMessageJson);
        }
    }

    @And("a request for the next commit is sent to the server with the next commit information provided previously")
    @ExecInitStep(value = CommitDiffInit.class)
    @AssertStep(value = NextCommitAssert.class, doFnFirst = true)
    public void nextCommitIsSentToTheServerWithTheNextCommitInformationProvidedPrevious() {
        var nextCommitRetrieved = commitDiff.callGraphQlQuery(repoOpInit.toCommitRequestArgs());
        assertions.assertSoftly(nextCommitRetrieved.isOk(), "Next commit waws not OK: %s"
                .formatted(nextCommitRetrieved.e().firstOptional().orElse(null)), "Next commit info present.");
        nextCommitRetrieved.r()
                .ifPresent(nc -> nextCommit.getNextCommitInfo().swap(new NextCommitAssert.NextCommitMetadata(nc)));
    }

    @Then("the response from retrieving next commit can be applied to the repository as a git diff")
    @AssertStep(value = {NextCommitAssert.class})
    public void nextCommitCanBeAppliedToGitDiff() {
        var repoData = repoOpInit.repoDataOrThrow();
        assertions.assertSoftly(nextCommit.getNextCommitInfo().isPresent(), "Not present", "Next commit info present.");
        nextCommit.getNextCommitInfo().res()
                .map(NextCommitAssert.NextCommitMetadata::nc)
                .ifPresent(ncm -> {
                    // apply commit to the repository for observation
                    // or for then pulling that out as staged information for validation
                    var applied = new GitHandlerActions(Paths.get(repoData.url()), commitDiffContextMapper)
                            .applyCommit(ncm);

                    assertions.assertSoftly(applied.isOk(), "Failed to apply git commit.", "Applied successfully.");
                });
    }

}
