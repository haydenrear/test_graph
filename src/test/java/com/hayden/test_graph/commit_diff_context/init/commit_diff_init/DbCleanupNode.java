package com.hayden.test_graph.commit_diff_context.init.commit_diff_init;

import com.hayden.commitdiffmodel.entity.CommitDiff;
import com.hayden.commitdiffmodel.repo.*;
import com.hayden.test_graph.commit_diff_context.config.DatabaseConfigProps;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.Callable;

@Component
@Slf4j
public class DbCleanupNode implements CommitDiffInitNode {

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
    private BlameTreeRepository blameTreeRepository;
    @Autowired
    private CommitDiffClusterRepository commitDiffClusterRepository;
    @Autowired
    private RepoExecutor repoExecutor;
    @Autowired
    private BlameNodeRepository blameNodeRepository;
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

            if (blameTreeRepository.count() > 0)
                blameTreeRepository.deleteAll();
            if (blameNodeRepository.count() > 0)
                blameNodeRepository.deleteAll();
            if (codeBranchRepository.count() > 0)
                codeBranchRepository.deleteAll();
            if (codeRepoRepository.count() > 0)
                codeRepoRepository.deleteAll();

            List<CommitDiff> toDelete = commitDiffRepository.findAll().stream()
                    .peek(cd -> cd.getPartials().clear())
                    .distinct()
                    .toList();

            if (!toDelete.isEmpty())
                commitDiffRepository.saveAll(toDelete);
            if (commitDiffItemRepository.count() > 0)
                commitDiffItemRepository.deleteAll();

            if(commitRepository.count() > 0)
                commitRepository.deleteAll();
            if (commitDiffClusterRepository.count() > 0)
                commitDiffClusterRepository.deleteAll();
            if (commitDiffRepository.count() > 0)
                commitDiffRepository.deleteAll();
            return null;
        });
        return c;
    }


}
