package com.hayden.test_graph.meta.exec;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.exec.prog_bubble.ProgExec;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.meta.LazyMetaGraphDelegate;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.ctx.MetaProgCtx;
import com.hayden.test_graph.report.ReportingValidationNode;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.sort.GraphSort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
@ResettableThread
public class MetaProgExec implements ProgExec {

    @Autowired
    @ResettableThread
    private MetaProgCtx metaProgCtx;

    @Autowired
    @Lazy
    private LazyMetaGraphDelegate lazyMetaGraphDelegate;

    @Autowired
    @ResettableThread
    private Assertions assertions;

    @Autowired(required = false)
    private List<ReportingValidationNode> reportingValidationNodes = new ArrayList<>();

    private final Queue<Class<? extends TestGraphContext>> registered = new ArrayDeque<>();

    private final Queue<Class<? extends TestGraphContext>> executed = new ArrayDeque<>();

    @Override
    public MetaCtx collectCtx() {
        // not used currently as this is the hypergraph.
        return this.execAll();
    }

    @Override
    public MetaCtx exec(Class<? extends TestGraphContext> ctx, MetaCtx metaCtx) {
        for (var hgNode : lazyMetaGraphDelegate.parseHyperGraph(ctx)) {
            MetaCtx finalMetaCtx = metaCtx;
            Class<? extends TestGraphContext<HyperGraphContext>> context = (Class<? extends TestGraphContext<HyperGraphContext>>) ctx;
            Stream<Class<? extends TestGraphContext>> contextsRetrieved = contextsPlanned(ctx, hgNode);
            HyperGraphExec<TestGraphContext<HyperGraphContext>, HyperGraphContext> hgGraphExec = hgNode;
            var done = contextsRetrieved
                    .map(c -> {
                        String msg = "Executing %s".formatted(c.getName());
                        assertions.assertSoftly(Objects.nonNull(c), msg, msg);
                        var ctxCreated = hgGraphExec.exec((Class<? extends TestGraphContext<HyperGraphContext>>) c, finalMetaCtx);
                        MetaCtx m = ctxCreated.bubbleMeta(finalMetaCtx);
                        executed.add(c);
                        executed.add(ctxCreated.getClass());
                        return Map.entry(m, ctxCreated);
                    })
                    .toList();


            done.forEach(mc -> {
                        if (mc.getKey() instanceof MetaProgCtx m) {
                            m.push(mc.getKey());
                            m.push(mc.getValue());
                        }
                    });
        }

        return metaCtx;
    }

    private Stream<Class<? extends TestGraphContext>> contextsPlanned(Class<? extends TestGraphContext> ctx, HyperGraphExec hgNode) {
        Stream<Class<? extends TestGraphContext>> contextsRetrieved = lazyMetaGraphDelegate.parseSubGraph(hgNode, ctx);
        return contextsRetrieved;
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
    public MetaCtx execAll() {
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

        for (var o : lazyMetaGraphDelegate.sort(ordering)) {
            log.info("Executing {}", o.getName());
            var next = this.exec(o);
            log.info("Executed {} - Result: {}", o.getName(), next.bubbleClazz().getName());
        }

        return this.metaProgCtx;
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

    @Override
    public MetaCtx exec(Class<? extends TestGraphContext> ctx) {
        var n = exec(ctx, metaProgCtx);
        assertDeps(ctx);
        return n;
    }

    /**
     * For false negatives
     * @param ctx
     */
    private void assertDeps(Class<? extends TestGraphContext> ctx) {
        var c = lazyMetaGraphDelegate.getGraphContext(ctx);
        assertions.assertSoftly(c.isPresent(), "Test graph context %s existed.", ctx.getName());
        assertions.assertSoftly(c.get().requiredDependencies().stream()
                        .allMatch(contextValue ->((ContextValue) contextValue).isPresent()),
                "All required dependencies were set.");
    }
}
