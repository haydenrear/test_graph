package com.hayden.test_graph.commit_diff_context.service;

import com.hayden.test_graph.commit_diff_context.ctx.CommitDiffInit;
import com.hayden.test_graph.thread.ThreadScope;
import lombok.Builder;
import org.intellij.lang.annotations.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CommitDiffContext {

    @Autowired
    @ThreadScope
    CommitDiffInit commitDiffInit;

    @Autowired
    HttpSyncGraphQlClient graphQlClient;

    private static final @Language("graphql") String NEXT_COMMIT_GRAPH_QL_TEMPLATE = """
                    mutation {
                        doRequest(
                            gitRepoPromptingRequest: {
                                    branchName: "%s",
                                    gitRepo: {path: "%s"} ,
                                    commitMessage: {value: "%s"},
                                    ragOptions: {commitsPerK: 3, topK: 3, maxDepth: 3}
                                  }
                            ) {
                            diffs {
                                diffType
                            },
                            commitMessage
                        }
                }
    """;

    private static final @Language("graphql") String ADD_CODE_BRANCH_TEMPLATE = """
                    mutation {
                        doGit(
                            gitOperation: {
                                    operation: ADD_BRANCH,
                                    gitBranch: {branch: "%s"} ,
                                    gitRepo: {path: "%s"}
                                  }
                            ) {
                            branch,
                            url,
                            repoStatus,
                            error {
                                message
                            }
                        }
                }
    """;

    @Builder
    public record CommitRequestArgs(String branchName, String gitRepoPath, String commitMessage) {}

    @Builder
    public record AddCodeBranchArgs(String branchName, String gitRepoPath, String commitMessage) {}

    public void requestCommit(CommitRequestArgs commitRequestArgs) {
        String document = NEXT_COMMIT_GRAPH_QL_TEMPLATE.formatted(commitRequestArgs.branchName, commitRequestArgs.gitRepoPath, commitRequestArgs.commitMessage);
        var found = graphQlClient.document(document)
                .executeSync()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public void addCodeBranch(AddCodeBranchArgs commitRequestArgs) {
        String document = ADD_CODE_BRANCH_TEMPLATE.formatted(commitRequestArgs.branchName, commitRequestArgs.gitRepoPath);
        var found = graphQlClient.document(document)
                .executeSync()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
