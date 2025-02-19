package com.hayden.test_graph.commit_diff_context.init.commit_diff_init;

import com.hayden.commitdiffmodel.repo.*;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public CommitDiffInit exec(CommitDiffInit c, MetaCtx h) {
        repoExecutor.perform(() -> {
            commitDiffItemRepository.deleteAll();
            codeBranchRepository.deleteAll();
            codeRepoRepository.deleteAll();

            commitDiffRepository.saveAllAndFlush(
                    commitDiffRepository.findAll().stream()
                            .peek(cd -> cd.getPartials().clear())
                            .distinct()
                            .toList());

            blameTreeRepository.deleteAll();
            blameNodeRepository.deleteAll();
            commitRepository.deleteAll();
            commitDiffRepository.deleteAll();
            commitDiffClusterRepository.deleteAll();
            return null;
        });
        return c;
    }


}
