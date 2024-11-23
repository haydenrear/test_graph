package com.hayden.test_graph.assert_g.exec;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.assert_g.ctx.AssertCtx;
import com.hayden.test_graph.assert_g.graph.AssertBubbleGraph;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.data_dep.exec.DataDepBubbleExec;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.graph.edge.EdgeExec;
import com.hayden.test_graph.graph.node.HyperGraphBubbleNode;
import com.hayden.test_graph.graph.node.TestGraphNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ResettableThread
public class AssertBubbleExec implements HyperGraphExec<AssertCtx, AssertBubble> {

    public interface BubblePreMapper extends GraphExecMapper<AssertBubble, AssertBubble> {}
    public interface BubblePostMapper extends GraphExecMapper<AssertBubble, AssertBubble> {}

    @Autowired(required = false)
    List<BubblePreMapper> preMappers;
    @Autowired(required = false)
    List<BubblePostMapper> postMappers;
    @Autowired
    @ResettableThread
    AssertExec initExec;
    @Autowired
    @ResettableThread
    AssertBubbleGraph bubbleGraph;

    @Autowired
    EdgeExec edgeExec;

    @Override
    public List<BubblePreMapper> preMappers() {
        return Optional.ofNullable(preMappers).orElse(new ArrayList<>());
    }

    @Override
    public List<BubblePostMapper> postMappers() {
        return Optional.ofNullable(postMappers).orElse(new ArrayList<>());
    }

    @Override
    public List<? extends HyperGraphBubbleNode<AssertBubble>> sortedNodes() {
        return bubbleGraph.sortedNodes();
    }

    @Override
    @Idempotent
    public AssertBubble exec(Class<? extends AssertCtx> ctx, MetaCtx prev) {
        var collected = this.initExec.collectCtx(ctx, prev);
        var c = edgeExec.postReducePreExecTestGraph(this, collected, prev);
        prev = edgeExec.postReducePreExecMetaCtx(this, collected, prev);
        collected = c.preMap(collected, prev);
        collected = c.exec(collected, prev);
        return c.collectCtx(collected);
    }

    @Override
    public AssertBubble exec(Class<? extends AssertCtx> ctx) {
        return this.exec(ctx, null);
    }

    @Override
    public Class<? extends AssertBubble> clzz() {
        return AssertBubble.class;
    }

    @Override
    public List<Class<? extends HyperGraphBubbleNode<? extends HyperGraphContext>>> dependsOn() {
        return List.of();
    }

    @Override
    public AssertBubble exec(AssertBubble c, MetaCtx metaCtx) {
        c = preMap(c, metaCtx);
        c = execInner(c, metaCtx);
        c = postMap(c, metaCtx);
        return c;
    }

    private AssertBubble execInner(AssertBubble c, MetaCtx metaCtx) {
        for (var b : bubbleGraph.sortedNodes()) {
            if (c.executableFor(b))
                c = b.exec(c, metaCtx);
        }
        return c;
    }
}
