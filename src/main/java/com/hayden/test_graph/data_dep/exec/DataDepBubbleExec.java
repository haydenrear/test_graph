package com.hayden.test_graph.data_dep.exec;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.data_dep.ctx.DataDepCtx;
import com.hayden.test_graph.data_dep.graph.DataDepBubbleGraph;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.graph.edge.GraphEdges;
import com.hayden.test_graph.graph.node.HyperGraphNode;
import com.hayden.test_graph.graph.node.TestGraphNode;
import com.hayden.test_graph.init.exec.InitBubbleExec;
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
public class DataDepBubbleExec implements HyperGraphExec<DataDepCtx, DataDepBubble, MetaCtx> {

    public interface BubblePreMapper extends GraphExecMapper<DataDepBubble, MetaCtx> {}

    public interface BubblePostMapper extends GraphExecMapper<DataDepBubble, MetaCtx> {}

    @Autowired(required = false)
    List<DataDepBubbleExec.BubblePreMapper> preMappers;
    @Autowired(required = false)
    List<DataDepBubbleExec.BubblePostMapper> postMappers;
    @Autowired
    @ThreadScope
    DataDepExec initExec;
    @Autowired
    @ThreadScope
    DataDepBubbleGraph bubbleGraph;

    @Autowired
    GraphEdges graphEdges;

    @Override
    public List<DataDepBubbleExec.BubblePreMapper> preMappers() {
        return Optional.ofNullable(preMappers).orElse(new ArrayList<>());
    }

    @Override
    public List<DataDepBubbleExec.BubblePostMapper> postMappers() {
        return Optional.ofNullable(postMappers).orElse(new ArrayList<>());
    }


    @Override
    public MetaCtx collectCtx(DataDepBubble toCollect) {
        return toCollect.bubble();
    }


    @Override
    @Idempotent
    public MetaCtx exec(Class<? extends DataDepCtx> ctx, MetaCtx prev) {
        var collected = this.initExec.collectCtx(ctx, prev);
        var c = graphEdges.addEdge(this, collected, prev);
        collected = c.preMap(collected, prev);
        collected = c.exec(collected, prev);
        return c.collectCtx(collected);
    }

    @Override
    public MetaCtx exec(Class<? extends DataDepCtx> ctx) {
        return this.exec(ctx, null);
    }

    @Override
    public DataDepBubble preMap(DataDepBubble ctx, MetaCtx metaCtx) {
        for (var r : preMappers()) {
            ctx = r.apply(ctx, metaCtx);
        }
        return ctx;
    }

    @Override
    public DataDepBubble postMap(DataDepBubble ctx, MetaCtx metaCtx) {
        for (var r : postMappers()) {
            ctx = r.apply(ctx, metaCtx);
        }
        for (var b : bubbleGraph.sortedNodes()) {
            ctx = b.postMap(ctx, metaCtx);
        }
        return ctx;
    }

    @Override
    public Class<? extends DataDepBubble> clzz() {
        return DataDepBubble.class;
    }

    @Override
    public List<Class<? extends TestGraphNode<DataDepBubble, MetaCtx>>> dependsOn() {
        return List.of();
    }


    @Override
    public DataDepBubble exec(DataDepBubble c, MetaCtx metaCtx) {
        c = preMap(c, metaCtx);
        c = execInner(c, metaCtx);
        c = postMap(c, metaCtx);
        return c;
    }

    private DataDepBubble execInner(DataDepBubble c, MetaCtx metaCtx) {
        for (var b : bubbleGraph.sortedNodes()) {
            if (c.executableFor(b))
                c = b.exec(c, metaCtx);
        }
        return c;
    }

    @Override
    public List<Class<? extends HyperGraphNode<? extends HyperGraphContext<MetaCtx>, MetaCtx>>> dependsOnHyperNodes() {
        return List.of(InitBubbleExec.class);
    }
}
