package com.hayden.test_graph.commit_diff_context.hg;

import com.hayden.test_graph.commit_diff_context.init.mountebank.CdMbInitCtx;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpBubble;
import com.hayden.test_graph.graph.edge.PreExecTestGraphEdge;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.ctx.MetaProgCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Slf4j
@Component
@ResettableThread
public class CommitDiffInitMountebankEdge implements PreExecTestGraphEdge<CdMbInitCtx, InitBubble> {

    @Override
    public CdMbInitCtx edge(CdMbInitCtx transform, MetaCtx s) {
        if (s instanceof MetaProgCtx second) {
            var i = second.retrieveBubbled(RepoOpBubble.class)
                    .toList();

            if (i.size() != 1)  {
                throw new RuntimeException("Failed to find commit diff init bubble: %s.".formatted(i));
            }

            i.stream().findAny()
                    .flatMap(c -> c.repositoryData().res().optional())
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
        return transform;
    }

    @Override
    public Predicate<? super Object> from() {
        return  c -> c instanceof CdMbInitCtx;
    }

    @Override
    public Predicate<? super Object> to() {
        return c -> c instanceof MetaProgCtx;
    }
}
