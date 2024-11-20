package com.hayden.test_graph.meta.exec;

import com.google.common.collect.Queues;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.GraphContext;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.graph.SubGraph;
import com.hayden.test_graph.graph.edge.EdgeExec;
import com.hayden.test_graph.exec.prog_bubble.ProgExec;
import com.hayden.test_graph.meta.LazyMetaGraphDelegate;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.ctx.MetaProgCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ResettableThread
public class MetaProgExec implements ProgExec {

    @Autowired
    @ResettableThread
    private MetaProgCtx metaProgCtx;

    @Autowired
    private EdgeExec edgeExec;

    @Autowired @Lazy
    LazyMetaGraphDelegate lazyMetaGraphDelegate;

    private Queue<Class<? extends TestGraphContext>> registered = new ArrayDeque<>();

    @Override
    public MetaCtx collectCtx() {
//        MetaCtx metaCtx = null;


//        for (var s : subGraphs) {
//            metaCtx = exec(s.clazz(), metaCtx);
//        }

//        return metaCtx;
        throw new RuntimeException("""
                Subgraphs need to be sorted and then iterated from beginning if collectCtx() is implemented, which if idempotent then it just finishes the
                ones not completed for this thread.
                """);
    }

    @Override
    public MetaCtx exec(Class<? extends TestGraphContext> ctx, MetaCtx metaCtx) {
//        assertDeps(ctx);
        for (var hgNode : lazyMetaGraphDelegate.retrieveHyperGraphDependencyGraph(ctx)) {
            MetaCtx finalMetaCtx = metaCtx;
            lazyMetaGraphDelegate.retrieveContextsToRun(hgNode, ctx)
                    .map(c -> {
                        HyperGraphExec<TestGraphContext, HyperGraphContext<MetaCtx>, MetaCtx> hgGraphExec
                                = edgeExec.preExecHgExecEdges(hgNode, finalMetaCtx);
                        var ctxCreated = hgGraphExec.exec(c, finalMetaCtx);
                        return edgeExec.postExecMetaCtxEdges(ctxCreated, finalMetaCtx);
                    })
                    .forEach(mc -> {
                        if (metaCtx instanceof MetaProgCtx m) {
                            m.push(mc);
                        }
                    });

            return finalMetaCtx;
        }

        return metaCtx;
    }

    @Override
    public void register(Class<? extends TestGraphContext> ctx) {
        this.registered.offer(ctx);
    }

    @Override
    public void execAll() {
        Class<? extends TestGraphContext> ctx;
        while((ctx = registered.poll()) != null) {
            this.exec(ctx);
        }
    }

    /**
     * For false negatives
     * @param ctx
     */
    private void assertDeps(Class<? extends TestGraphContext> ctx) {
        var c = lazyMetaGraphDelegate.getGraphContext(ctx);
        assert c.isPresent();
        assert c.get().requiredDependencies().stream().allMatch(contextValue ->((ContextValue) contextValue).isPresent());
    }

    @Override
    public MetaCtx exec(Class<? extends TestGraphContext> ctx) {
        var n = exec(ctx, metaProgCtx);
        return n;
    }

}
