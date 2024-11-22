package com.hayden.test_graph.init.exec;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.graph.edge.EdgeExec;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.graph.node.HyperGraphTestNode;
import com.hayden.test_graph.graph.node.TestGraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.graph.InitBubbleGraph;
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
public class InitBubbleExec implements HyperGraphExec<InitCtx, InitBubble> {

    public interface BubblePreMapper extends GraphExecMapper<InitBubble, InitBubble> {}
    public interface BubblePostMapper extends GraphExecMapper<InitBubble, InitBubble> {}

    @Autowired(required = false)
    List<BubblePreMapper> preMappers;
    @Autowired(required = false)
    List<BubblePostMapper> postMappers;
    @Autowired
    @ResettableThread
    InitExec initExec;
    @Autowired
    @ResettableThread
    InitBubbleGraph bubbleGraph;

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
    public InitBubble collectCtx(InitBubble toCollect) {
        return toCollect.bubble();
    }

    @Override
    @Idempotent
    public InitBubble exec(Class<? extends InitCtx> ctx, MetaCtx prev) {
        var collected = this.initExec.collectCtx(ctx, prev);
        var c = edgeExec.postReducePreExecTestGraph(this, collected, prev);
        prev = edgeExec.postReducePreExecMetaCtx(this, collected, prev);
        collected = c.preMap(collected, prev);
        collected = c.exec(collected, prev);
        var collectedCtx =  c.collectCtx(collected);
        return edgeExec.postExecHgEdges(this, collectedCtx, prev);
    }

    @Override
    public InitBubble exec(Class<? extends InitCtx> ctx) {
        return this.exec(ctx, null);
    }

    @Override
    public InitBubble preMap(InitBubble ctx, MetaCtx metaCtx) {
        for (var r : preMappers()) {
            ctx = r.apply(ctx, metaCtx);
        }
        return ctx;
    }

    @Override
    public InitBubble postMap(InitBubble ctx, MetaCtx metaCtx) {
        for (var r : postMappers()) {
            ctx = r.apply(ctx, metaCtx);
        }
        for (var b : bubbleGraph.sortedNodes()) {
            ctx = b.preMap(ctx, metaCtx);
        }
        return ctx;
    }

    @Override
    public Class<? extends InitBubble> clzz() {
        return InitBubble.class;
    }

    @Override
    public List<Class<? extends HyperGraphTestNode<InitBubble>>> dependsOn() {
        return List.of();
    }


    @Override
    public InitBubble exec(InitBubble c, MetaCtx metaCtx) {
        c = preMap(c, metaCtx);
        c = execInner(c, metaCtx);
        c = postMap(c, metaCtx);
        return c;
    }

    private InitBubble execInner(InitBubble c, MetaCtx metaCtx) {
        for (var b : bubbleGraph.sortedNodes()) {
            if (c.executableFor(b))
                c = b.exec(c, metaCtx);
        }
        return c;
    }
}
