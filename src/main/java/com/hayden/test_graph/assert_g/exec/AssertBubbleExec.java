package com.hayden.test_graph.assert_g.exec;

import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.assert_g.ctx.AssertCtx;
import com.hayden.test_graph.assert_g.graph.AssertBubbleGraph;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.data_dep.exec.DataDepBubbleExec;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.graph.node.HyperGraphBubbleNode;
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

    @Override
    public boolean is(Class<? extends TestGraphContext> isThis) {
        return AssertCtx.class.isAssignableFrom(isThis)
                || AssertBubble.class.isAssignableFrom(isThis);
    }

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
        if (collected.skip())
            return collected;
        collected = preMap(collected, prev);
        collected = exec(collected, prev);
        return collectCtx(collected);
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
    public List<Class<? extends HyperGraphExec>> dependsOn() {
        return List.of(InitBubbleExec.class, DataDepBubbleExec.class);
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
            if (c.executableFor(b) && !c.skip())
                c = b.exec(c, metaCtx);
        }
        return c;
    }
}
