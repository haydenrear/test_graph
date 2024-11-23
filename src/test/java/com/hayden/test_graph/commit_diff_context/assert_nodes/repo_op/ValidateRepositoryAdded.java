package com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ResettableThread
public class ValidateRepositoryAdded implements RepoOpAssertNode {

    @Autowired
    private Assertions assertions;
    @Autowired
    private CommitDiff commitDiff;

    @Override
    public Class<? extends RepoOpAssertCtx> clzz() {
        return RepoOpAssertCtx.class;
    }

    @Override
    @Idempotent(returnArg = 0)
    public RepoOpAssertCtx exec(RepoOpAssertCtx c, MetaCtx h) {
        c.getRepositoryAssertionDescriptor()
                .res()
                .flatMap(rad -> {
                    return c.repoUrl().res()
                            .filterResult(rd -> rd.branchName() != null && rd.url() != null)
                            .map(rd -> Map.entry(rd, rad));
                })
                .ifPresent(radItem -> {
                    var q = commitDiff.callGraphQlQuery(
                            CommitDiff.ValidateBranchAdded.builder()
                                    .gitRepoPath(radItem.getKey().url())
                                    .branchName(radItem.getKey().branchName())
                                    .build());
                    assertions.assertThat(q.e().isEmpty())
                            .withFailMessage("There existed an error on the GraphQl callback.")
                            .isTrue();
                    // TODO: retrieve.
                });
        return c;
    }
}
