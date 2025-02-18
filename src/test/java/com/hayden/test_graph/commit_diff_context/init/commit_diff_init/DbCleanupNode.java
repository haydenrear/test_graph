package com.hayden.test_graph.commit_diff_context.init.commit_diff_init;

import com.hayden.commitdiffmodel.entity.CommitDiffContextBlameTree;
import com.hayden.commitdiffmodel.repo.*;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInit;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Override
    public CommitDiffInit exec(CommitDiffInit c, MetaCtx h) {
        commitDiffItemRepository.deleteAll();
        codeBranchRepository.deleteAll();
        codeRepoRepository.deleteAll();
        blameTreeRepository.deleteAll();
        commitDiffClusterRepository.deleteAll();
        commitRepository.deleteAll();
        commitDiffRepository.deleteAll();
        return c;
    }

    @Override
    public Class<CommitDiffInit> clzz() {
        return CommitDiffInit.class;
    }
}
