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
import com.hayden.test_graph.report.ReportingValidationNode;
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
    private LazyMetaGraphDelegate lazyMetaGraphDelegate;

    @Autowired(required = false)
    private List<ReportingValidationNode> reportingValidationNodes = new ArrayList<>();

    private final Queue<Class<? extends TestGraphContext>> registered = new ArrayDeque<>();

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
            // TODO partition these by bubble type, then merge, then push result onto MetaProgCtx,
            //      then single context pushed for MetaProgCtx for each bubble type,
            //      > InitBubble, > AssertBubble, etc.
            lazyMetaGraphDelegate.retrieveContextsToRun(hgNode, ctx)
                    .map(c -> {
                        HyperGraphExec<TestGraphContext<HyperGraphContext>, HyperGraphContext> hgGraphExec
                                = edgeExec.preExecHgExecEdges(hgNode, finalMetaCtx);
                        var ctxCreated = hgGraphExec.exec((Class<? extends TestGraphContext<HyperGraphContext>>) c, finalMetaCtx);
                        var m = edgeExec.postExecMetaCtxEdges(ctxCreated.bubbleMeta(finalMetaCtx), finalMetaCtx);
                        return Map.entry(m, ctxCreated);
                    })
                    .forEach(mc -> {
                        if (mc.getKey() instanceof MetaProgCtx m) {
                            m.push(mc.getKey());
                            m.push(mc.getValue());
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

    /**
     * Gets called on the first assert step - this removes the previous contexts registered one at a time,
     * executing them first, running each one only once, in the ordering of the last one inserted is kept,
     * assuming that it has dependencies on anything before it.
     */
    @Override
    public void execAll() {
        Class<? extends TestGraphContext> ctx;

        List<Class<? extends TestGraphContext>> ordering = new ArrayList<>();

        while((ctx = registered.poll()) != null) {
            if (ordering.contains(ctx)) {
                // the ordering should be last so that anything dependent will run first
                ordering.remove(ctx);
                assert !ordering.contains(ctx);
                ordering.add(ctx);
            } else {
                ordering.add(ctx);
            }
        }

        for (var o : ordering) {
            this.exec(o);
        }
    }

    @Override
    public int didExec() {
        var s = this.metaProgCtx.size();
        this.metaProgCtx.stream()
                .forEach(mc -> reportingValidationNodes.stream()
                        .filter(rn -> rn.matches(mc))
                        .forEach(rn -> rn.doValidateReport(mc)));
        return s;
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
