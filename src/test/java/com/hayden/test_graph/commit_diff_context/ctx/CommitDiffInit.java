package com.hayden.test_graph.commit_diff_context.ctx;

import com.hayden.test_graph.commit_diff_context.init.CommitDiffInitNode;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.thread.ThreadScope;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ThreadScope
public record CommitDiffInit(
        ContextValue<RepositoryData> repoData,
        ContextValue<UserCodeData> userCodeData,
        ContextValue<BubbleData> bubbleDataContextValue
) implements InitCtx {

    @Builder
    public record RepositoryData(String url, String branchName) {}

    @Builder
    public record UserCodeData(String commitMessage) {}

    @Builder
    public record BubbleData(Path clonedTo) {}

    public CommitDiffInit() {
        this(ContextValue.empty(), ContextValue.empty(),
                ContextValue.empty());
    }

    @Override
    public CommitDiffInitBubble bubble() {
        return new CommitDiffInitBubble();
    }

    @Override
    public Class<? extends InitBubble> bubbleClazz() {
        return CommitDiffInitBubble.class;
    }

    @Override
    public boolean executableFor(GraphNode n) {
        return n instanceof CommitDiffInitNode;
    }

    @Override
    public boolean toSet(TestGraphContext context) {
        return false;
    }

    @Override
    public void doSet(TestGraphContext context) {

    }


}
