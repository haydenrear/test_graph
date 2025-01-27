package com.hayden.test_graph.commit_diff_context.hg;

import com.hayden.test_graph.commit_diff_context.init.mountebank.CdMbInitCtx;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpBubble;
import com.hayden.test_graph.graph.edge.FromBubbleEdgeInit;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ResettableThread
public class CommitDiffInitMountebankEdge implements FromBubbleEdgeInit<CdMbInitCtx, RepoOpBubble> {

    @Override
    public void set(CdMbInitCtx transform, RepoOpBubble setFrom) {
        setFrom.repositoryData().res().optional()
                .ifPresentOrElse(bub -> {
                    transform.bubble().getCommitDiffContextValue().addRepo()
                            .getGitRepo().setPath(bub.url());
                    transform.bubble().getCommitDiffContextValue().repositoryRequest()
                            .getGitRepo().setPath(bub.url());
                    transform.bubble().getCommitDiffContextValue().repositoryRequest()
                            .getGitBranch().setBranch(bub.url());
                    transform.bubble().getCommitDiffContextValue().addRepo()
                            .getGitRepo().setPath(bub.url());
                    transform.bubble().getCommitDiffContextValue().addRepo()
                            .setBranchName(bub.branchName());
                }, () -> log.warn("Did not find any source of commit diff context value."));

    }

    @Override
    public Class<RepoOpBubble> bubbleClazz() {
        return RepoOpBubble.class;
    }

    @Override
    public Class<CdMbInitCtx> initClazz() {
        return CdMbInitCtx.class;
    }


}
