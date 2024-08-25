package com.hayden.test_graph.meta.exec;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.SubGraph;
import com.hayden.test_graph.graph.edge.GraphEdges;
import com.hayden.test_graph.exec.prog_bubble.ProgExec;
import com.hayden.test_graph.graph.service.MetaGraphDelegate;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.ctx.MetaProgCtx;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MetaProgExec implements ProgExec {

    @Autowired
    @ThreadScope
    private MetaProgCtx metaProgCtx;

    @Autowired
    private GraphEdges graphEdges;

    @Autowired
    private List<SubGraph>  subGraphs;

    @Autowired @Lazy
    MetaGraphDelegate metaGraphDelegate;

    @Autowired
    private TestGraphSort graphSort;

    @Override
    public MetaCtx collectCtx() {
        MetaCtx metaCtx = null;

        for (var s : subGraphs) {
            metaCtx = exec(s.clazz(), metaCtx);
        }

        return metaCtx;
    }

    @Override
    public MetaCtx exec(Class<? extends TestGraphContext> ctx, MetaCtx metaCtx) {
        for (var hgNode : metaGraphDelegate.retrieveHyperGraphDependencyGraph(ctx)) {
            MetaCtx finalMetaCtx = metaCtx;
            metaCtx = Optional.ofNullable(metaGraphDelegate.getMatchingContext(hgNode))
                    .map(c -> {
                        var n = graphEdges.addEdge(hgNode, finalMetaCtx);
                        return (MetaCtx) n.exec(c, finalMetaCtx);
                    })
                    .orElseGet(() -> {
                        log.error("Did not find matching context for {}.", hgNode.getClass().getName());
                        return finalMetaCtx;
                    });
        }

        return metaCtx;
    }
    @Override
    public MetaCtx exec(Class<? extends TestGraphContext> ctx) {
        if (!metaProgCtx.isEmpty()) {
            var prev = metaProgCtx.peek();
            var metaCtx = exec(ctx, prev);
            metaProgCtx.push(metaCtx);
            return metaCtx;
        }

        var n = exec(ctx, null);
        metaProgCtx.push(n);
        return n;
    }
}
