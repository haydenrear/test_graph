package com.hayden.test_graph.commit_diff_context.service;

import org.intellij.lang.annotations.Language;

public interface GraphQlTemplates {
    @Language("graphql")
    String NEXT_COMMIT_GRAPH_QL_TEMPLATE = """
                            mutation {
                                doCommit(
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
    @Language("graphql")
    String ADD_CODE_BRANCH_TEMPLATE = """
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
    @Language("graphql")
    String RETRIEVE_CODE_BRANCH_TEMPLATE = """
                            query {
                                branch(gitRepo: {gitBranch: "%s", gitRepo: "%s"}) {
                                    branch,
                                    url,
                                    repoStatus,
                                    error
                                }
                            }
            """;
}
