package com.hayden.test_graph.commit_diff_context.assert_nodes.repo_op;

import com.hayden.commitdiffmodel.repo.CodeBranchRepository;
import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.service.CommitDiff;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@ResettableThread
public class ValidateRepositoryAdded implements RepoOpAssertNode {

    @Autowired
    private Assertions assertions;
    @Autowired
    private CommitDiff commitDiff;
    @Autowired
    private CodeBranchRepository codeBranchRepository;

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
                    return Result.fromOpt(
                            c.repoUrl()
                                    .filter(rd -> rad.branchToBeAdded() != null && rd.url() != null)
                                    .map(rd -> Map.entry(rd, rad)));
                })
                .ifPresent(radItem -> {
                    String branchName = Optional.ofNullable(radItem.getValue())
                            .map(RepoOpAssertCtx.RepoOpAssertionDescriptor::branchToBeAdded)
                            .orElse(radItem.getKey().branchName());

                    var foundBranch = codeBranchRepository.findByBranchNameWithParent(branchName,
                            radItem.getKey().url());
                    assertions.assertThat(foundBranch)
                            .isPresent();
                    foundBranch.ifPresent(branch -> assertions.assertThat(foundBranch.get().getBranchName())
                            .isEqualTo(branchName));
                });
        return c;
    }
}
