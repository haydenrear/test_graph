package com.hayden.test_graph.commit_diff_context.init.repo_op.ctx;

import com.hayden.commitdiffmodel.codegen.types.RagOptions;
import com.hayden.commitdiffmodel.codegen.types.UpdateHeadCtx;
import com.hayden.test_graph.ctx.ContextValue;

import java.util.Comparator;
import java.util.List;

public sealed interface RepoInitItem {

    Comparator<RepoInitItem> c = new Comparator<>() {
        static final List<Class<? extends RepoInitItem>> REPO_INIT_ORDERING = List.of(AddCodeBranch.class, AddEmbeddings.class, AddBlameNodes.class);

        @Override
        public int compare(RepoInitItem o1, RepoInitItem o2) {
            return Integer.compare(REPO_INIT_ORDERING.indexOf(o1.getClass()), REPO_INIT_ORDERING.indexOf(o2.getClass()));
        }
    };

    record AddCodeBranch(RepoOpInit.RepositoryData repositoryData) implements RepoInitItem {}

    record AddEmbeddings() implements RepoInitItem {}

    record AddBlameNodes() implements RepoInitItem {}

    record UpdateHeadNode(UpdateHeadCtx ctx) implements RepoInitItem {}

}
