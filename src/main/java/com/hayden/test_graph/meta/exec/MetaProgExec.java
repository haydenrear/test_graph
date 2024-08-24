package com.hayden.test_graph.meta.exec;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.edge.GraphEdges;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.exec.prog_bubble.ProgExec;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.ctx.MetaProgCtx;
import com.hayden.test_graph.meta.graph.MetaGraph;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MetaProgExec implements ProgExec {

    @Autowired
    @ThreadScope
    private MetaGraph metaGraph;

    @Autowired
    @ThreadScope
    private MetaProgCtx metaProgCtx;

    @Autowired
    private List<TestGraphContext> contexts;

    @Autowired
    private GraphEdges graphEdges;

    @Override
    public MetaCtx collectCtx() {
        // TODO: reduce with hypergraph edges.
        return null;
    }

    @Override
    public MetaCtx exec(Class<? extends TestGraphContext> ctx, MetaCtx metaCtx) {
        // TODO: here is where it will find converter/merger between contexts.
        //      previous contexts will be merged with current context as a way
        //      to add edges between the hypergraphs.
        for (var s : metaGraph.sortedNodes()) {
            return s.t().res()
                    .optional()
                    .flatMap(h -> {
                        if (h instanceof HyperGraphExec execNode) {
                            // note: MetaCtx can contain a history of the previous MetaCtx for arbitrary edge creation.
                            Optional.ofNullable(metaCtx).ifPresent(m -> graphEdges.addEdge(execNode, m));
                            return Optional.of((MetaCtx) execNode.exec(ctx, metaCtx));
                        }

                        return Optional.empty();
                    })
                    .orElse(null);
        }

        return null;
    }

    @Override
    public MetaCtx exec(Class<? extends TestGraphContext> ctx) {
        if (!metaProgCtx.isEmpty()) {
            var prev = metaProgCtx.pop();
            var metaCtx = exec(ctx, prev);
            metaProgCtx.push(metaCtx);
            return metaCtx;
        }

        var n = exec(ctx, null);
        metaProgCtx.push(n);
        return n;
    }
}
