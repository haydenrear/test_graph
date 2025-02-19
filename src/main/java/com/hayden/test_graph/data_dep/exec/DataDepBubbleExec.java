package com.hayden.test_graph.data_dep.exec;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.data_dep.ctx.DataDepCtx;
import com.hayden.test_graph.data_dep.graph.DataDepBubbleGraph;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.graph.edge.EdgeExec;
import com.hayden.test_graph.graph.node.HyperGraphBubbleNode;
import com.hayden.test_graph.graph.node.TestGraphNode;
import com.hayden.test_graph.init.exec.InitBubbleExec;
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
public class DataDepBubbleExec implements HyperGraphExec<DataDepCtx, DataDepBubble> {

    public interface BubblePreMapper extends GraphExecMapper<DataDepBubble, DataDepBubble> {}

    public interface BubblePostMapper extends GraphExecMapper<DataDepBubble, DataDepBubble> {}

    @Autowired(required = false)
    List<DataDepBubbleExec.BubblePreMapper> preMappers;
    @Autowired(required = false)
    List<DataDepBubbleExec.BubblePostMapper> postMappers;
    @Autowired
    @ResettableThread
    DataDepExec initExec;
    @Autowired
    @ResettableThread
    DataDepBubbleGraph bubbleGraph;

    @Autowired
    EdgeExec edgeExec;

    @Override
    public List<DataDepBubbleExec.BubblePreMapper> preMappers() {
        return Optional.ofNullable(preMappers).orElse(new ArrayList<>());
    }

    @Override
    public List<DataDepBubbleExec.BubblePostMapper> postMappers() {
        return Optional.ofNullable(postMappers).orElse(new ArrayList<>());
    }


    @Override
    @Idempotent
    public DataDepBubble exec(Class<? extends DataDepCtx> ctx, MetaCtx prev) {
        DataDepBubble collected = this.initExec.collectCtx(ctx, prev);
        if (collected.skip())
            return collected;
        var c = edgeExec.postReducePreExecTestGraph(this, collected, prev);
        prev = edgeExec.postReducePreExecMetaCtx(this, collected, prev);
        collected = c.preMap(collected, prev);
        collected = c.exec(collected, prev);
        return c.collectCtx(collected);
    }

    @Override
    public DataDepBubble exec(Class<? extends DataDepCtx> ctx) {
        return this.exec(ctx, null);
    }


    @Override
    public Class<? extends DataDepBubble> clzz() {
        return DataDepBubble.class;
    }

    @Override
    public List<Class<? extends TestGraphNode<DataDepBubble, DataDepBubble>>> dependsOn() {
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
            if (c.executableFor(b) && !c.skip())
                c = b.exec(c, metaCtx);
        }
        return c;
    }

    @Override
    public List<? extends HyperGraphBubbleNode<DataDepBubble>> sortedNodes() {
        return bubbleGraph.sortedNodes();
    }
}
