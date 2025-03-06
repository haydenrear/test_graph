package com.hayden.test_graph.commit_diff_context.init.commit_diff_init;

import com.hayden.commitdiffmodel.repo.*;
import com.hayden.test_graph.commit_diff_context.config.DatabaseConfigProps;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public boolean skip(CommitDiffInit t) {
        if (databaseConfigProps.isSkipDbCleanup())
            return true;

        return t.getSkipCleanupNode().optional()
                .orElse(false);
    }

    @Override
    @Transactional
    public CommitDiffInit exec(CommitDiffInit c, MetaCtx h) {
        repoExecutor.perform(() -> {


            commitDiffClusterRepository.removeAllCommitDiffs();
            commitDiffRepository.removeAllCommitDiffs();
            commitDiffRepository.removeAllCommitDiffsItems();
            commitDiffItemRepository.removeAllCommitDiffsItems();


            codeBranchRepository.deleteAllWithCommitsSet();
            commitRepository.deleteAllHeads();

            if (commitDiffRepository.count() > 0)
                commitDiffRepository.deleteAll();
            if (commitDiffItemRepository.count() > 0)
                commitDiffItemRepository.deleteAll();
            if (codeRepoRepository.count() > 0)
                codeRepoRepository.deleteAll();

            codeBranchRefRepository.deleteAll();

            if (commitDiffClusterRepository.count() > 0)
                commitDiffClusterRepository.deleteAll();

            return null;
        });
        return c;
    }


}
