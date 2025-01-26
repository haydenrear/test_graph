package com.hayden.test_graph.meta.exec;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.graph.edge.EdgeExec;
import com.hayden.test_graph.exec.prog_bubble.ProgExec;
import com.hayden.test_graph.meta.LazyMetaGraphDelegate;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.ctx.MetaProgCtx;
import com.hayden.test_graph.report.ReportingValidationNode;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

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

    private final Queue<Class<? extends TestGraphContext>> executed = new ArrayDeque<>();

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
        for (var hgNode : lazyMetaGraphDelegate.parseHyperGraph(ctx)) {
            MetaCtx finalMetaCtx = metaCtx;
            Stream<Class<? extends TestGraphContext>> contextsRetrieved = lazyMetaGraphDelegate.parseSubGraph(hgNode, ctx);
            contextsRetrieved.map(c -> {
                        HyperGraphExec<TestGraphContext<HyperGraphContext>, HyperGraphContext> hgGraphExec
                                = edgeExec.preExecHgExecEdges(hgNode, finalMetaCtx);
                        var ctxCreated = hgGraphExec.exec((Class<? extends TestGraphContext<HyperGraphContext>>) c, finalMetaCtx);
                        MetaCtx m = edgeExec.postExecMetaCtxEdges(ctxCreated.bubbleMeta(finalMetaCtx), finalMetaCtx);
                        executed.add(c);
                        executed.add(ctxCreated.getClass());
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
        if (this.registered.contains(ctx)) {
            // ensure idempotency of calling them
            return;
        }

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
                // the ordering should be first because it's assumed that the first time it's
                //  requested it's needed in the following
                // don't do anything
            } else if (!executed.contains(ctx)) {
                ordering.add(ctx);
            }
        }

        // Is there some sort of hypergraph sorting algorithm?
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
