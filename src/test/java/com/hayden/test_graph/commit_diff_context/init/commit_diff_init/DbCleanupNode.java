package com.hayden.test_graph.commit_diff_context.init.commit_diff_init;

import com.hayden.test_graph.commit_diff_context.config.DatabaseConfigProps;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DbCleanupNode implements CommitDiffInitNode {

    @Autowired
    private CodeBranchRefRepository codeBranchRefRepository;
    @Autowired
    private CodeBranchRepository codeBranchRepository;
    @Autowired
    private CodeRepoRepository codeRepoRepository;
    @Autowired
    private CommitRepository commitRepository;
    @Autowired
    private CommitDiffRepository commitDiffRepository;
    @Autowired
    private CommitDiffItemRepository commitDiffItemRepository;
    @Autowired
    private CommitDiffClusterRepository commitDiffClusterRepository;
    @Autowired
    private RepoExecutor repoExecutor;
    @Autowired
    private DatabaseConfigProps databaseConfigProps;
    @Autowired
    private EmbeddedGitDiffRepository embeddedGitDiffRepository;

    @Override
    public boolean skip(CommitDiffInit t) {
        if (databaseConfigProps.isSkipDbCleanup())
            return true;

        return t.getSkipCleanupNode().optional()
                .orElse(false);
    }

    @Override
    public CommitDiffInit exec(CommitDiffInit c, MetaCtx h) {

        codeBranchRepository.deleteAllCodeBranch();
        commitDiffClusterRepository.removeAllCommitDiffsAtOnce();
        commitRepository.deleteAllRecursive();

        return c;
    }


}
