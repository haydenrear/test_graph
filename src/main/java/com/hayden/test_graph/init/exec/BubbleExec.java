package com.hayden.test_graph.init.exec;

import com.hayden.test_graph.edge.GraphEdges;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.graph.TestGraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.graph.InitBubbleGraph;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ThreadScope
public class BubbleExec implements HyperGraphExec<InitCtx, InitBubble, MetaCtx> {

    public interface BubblePreMapper extends GraphExecMapper<InitBubble, MetaCtx> {}
    public interface BubblePostMapper extends GraphExecMapper<InitBubble, MetaCtx> {}

    @Autowired(required = false)
    List<BubblePreMapper> preMappers;
    @Autowired(required = false)
    List<BubblePostMapper> postMappers;
    @Autowired
    @ThreadScope
    InitExec initExec;
    @Autowired
    @ThreadScope
    InitBubbleGraph bubbleGraph;

    @Autowired
    GraphEdges graphEdges;

    @Override
    public List<BubblePreMapper> preMappers() {
        return Optional.ofNullable(preMappers).orElse(new ArrayList<>());
    }

    @Override
    public List<BubblePostMapper> postMappers() {
        return Optional.ofNullable(postMappers).orElse(new ArrayList<>());
    }


    @Override
    public MetaCtx collectCtx(InitBubble toCollect) {
        return toCollect.bubble();
    }

    @Override
    public MetaCtx exec(Class<? extends InitCtx> ctx, MetaCtx prev) {
        var collected = this.initExec.collectCtx(ctx, prev);
        collected = preMap(collected, prev);
        collected = exec(collected, prev);
        graphEdges.addEdge(this, collected, prev);
        return collectCtx(collected);
    }

    @Override
    public MetaCtx exec(Class<? extends InitCtx> ctx) {
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
    public List<Class<? extends TestGraphNode<InitBubble, MetaCtx>>> dependsOn() {
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
            c = b.exec(c, metaCtx);
        }
        return c;
    }
}
