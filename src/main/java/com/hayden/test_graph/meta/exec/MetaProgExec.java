package com.hayden.test_graph.meta.exec;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.SubGraph;
import com.hayden.test_graph.graph.edge.GraphEdges;
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
    private GraphEdges graphEdges;

    @Autowired
    private List<SubGraph>  subGraphs;

    @Autowired @Lazy
    LazyMetaGraphDelegate lazyMetaGraphDelegate;

    @Autowired
    private TestGraphSort graphSort;

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
        for (var hgNode : lazyMetaGraphDelegate.retrieveHyperGraphDependencyGraph(ctx)) {
            MetaCtx finalMetaCtx = metaCtx;
            lazyMetaGraphDelegate.retrieveContextsToRun(hgNode, ctx)
                    .map(c -> {
                        var n = graphEdges.preExecHgExecEdges(hgNode, finalMetaCtx);
                        var ctxCreated = (MetaCtx) n.exec(c, finalMetaCtx);
                        return graphEdges.postExecMetaCtxEdges(ctxCreated, finalMetaCtx);
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
    public MetaCtx exec(Class<? extends TestGraphContext> ctx) {
        var n = exec(ctx, metaProgCtx);
        return n;
    }

}
