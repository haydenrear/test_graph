package com.hayden.test_graph.meta.exec;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.SubGraph;
import com.hayden.test_graph.graph.edge.GraphEdges;
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

    @Autowired
    private List<SubGraph>  subGraphs;

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
        for (var s : metaGraph.sortedNodes()) {
            final MetaCtx nextMeta = metaCtx;
            metaCtx = s.t().res()
                    .optional()
                    .flatMap(h -> {
                        if (h instanceof HyperGraphExec execNode) {
                            // note: MetaCtx can contain a history of the previous MetaCtx for arbitrary edge creation.
                            return Optional.ofNullable(nextMeta)
                                    .map(m -> graphEdges.addEdge(execNode, m))
                                    .or(() -> Optional.of(execNode))
                                    .map(exec -> (MetaCtx) exec.exec(ctx, nextMeta));
                        }

                        return Optional.empty();
                    })
                    .orElse(null);
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
