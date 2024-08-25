package com.hayden.test_graph.data_dep.exec;

import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.data_dep.ctx.DataDepCtx;
import com.hayden.test_graph.data_dep.graph.DataDepGraph;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.edge.GraphEdges;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
@ThreadScope
public class DataDepExec implements GraphExec.ExecNode<DataDepCtx, DataDepBubble> {

    public interface DataDepReducer extends GraphExecReducer<DataDepCtx, DataDepBubble> {}

    public interface DataDepPreMapper extends GraphExecMapper<DataDepCtx, DataDepBubble> {

        interface PreClean extends DataDepPreMapper {

            boolean preCleanData(DataDepCtx dataDepCtx, MetaCtx metaCtx);

            @Override
            default DataDepCtx apply(DataDepCtx dataDepCtx, MetaCtx h) {
                if (!preCleanData(dataDepCtx, h))  {
                    // log
                }

                return dataDepCtx;
            }
        }

    }

    public interface DataDepPostMapper extends GraphExecMapper<DataDepCtx, DataDepBubble> {
    }

    @Autowired(required = false)
    List<DataDepExec.DataDepReducer> reducers;
    @Autowired(required = false)
    List<DataDepExec.DataDepPreMapper> preMappers;
    @Autowired(required = false)
    List<DataDepExec.DataDepPostMapper> postMappers;

    @Autowired
    GraphEdges graphEdges;

    @Autowired
    @ThreadScope
    DataDepGraph dataDepGraph;

    @Override
    public DataDepBubble exec(DataDepCtx initCtx, DataDepBubble prev, MetaCtx metaCtx) {
        var nodes = Optional.ofNullable(this.dataDepGraph.sortedNodes().get(initCtx.getClass()))
                .orElse(new ArrayList<>());
        var toExec = retrieveToExec(initCtx, prev, metaCtx);
        initCtx = toExec.preMap(initCtx, metaCtx, nodes);
        initCtx = toExec.exec(initCtx, metaCtx, nodes);
        final DataDepCtx initCtxExec = toExec.postMap(initCtx, metaCtx, nodes);
        return Optional.ofNullable(initCtxExec)
                .map(DataDepCtx::bubble)
                .stream()
                .findAny()
                .orElse(null);
    }

    private DataDepExec retrieveToExec(DataDepCtx initCtx, DataDepBubble prev, MetaCtx metaCtx) {
        return Optional.ofNullable(prev)
                .map(ib -> graphEdges.addEdge(this, initCtx, ib, metaCtx))
                .orElseGet(() -> graphEdges.addEdge(this, initCtx, metaCtx));
    }

    @Override
    public DataDepBubble exec(DataDepCtx initCtx, MetaCtx metaCtx) {
        return exec(initCtx, null, metaCtx);
    }

    @Override
    public List<DataDepExec.DataDepReducer> reducers() {
        return Optional.ofNullable(reducers).orElse(new ArrayList<>());
    }

    @Override
    public List<DataDepExec.DataDepPreMapper> preMappers() {
        return Optional.ofNullable(preMappers).orElse(new ArrayList<>());
    }

    @Override
    public List<? extends GraphExecMapper<DataDepCtx, DataDepBubble>> postMappers() {
        return Optional.ofNullable(postMappers).orElse(new ArrayList<>());
    }

    @Override
    public DataDepBubble collectCtx(Class<? extends DataDepCtx> toCollect, MetaCtx metaCtx) {
        List<? extends DataDepCtx> intCtx = this.dataDepGraph.sortedCtx(toCollect);
        if (intCtx.isEmpty()) {
            logBubbleError();
            return null;
        } else if (intCtx.size() == 1) {
            return intCtx.getFirst().bubble();
        } else {
            AtomicReference<DataDepBubble> prev = new AtomicReference<>();
            return GraphExec.chainCtx(
                            this.reducers(),
                            intCtx,
                            p -> doExec(metaCtx, p, prev)
                    )
                    .orElseGet(() -> {
                        logBubbleError();
                        return null;
                    });
        }
    }

    public DataDepCtx preMap(DataDepCtx initCtx, MetaCtx metaCtx, List<? extends GraphNode<DataDepCtx, DataDepBubble>> nodes) {
        for (var p : preMappers()) {
            initCtx = p.apply(initCtx, metaCtx);
        }
        final DataDepCtx initCtxExec = initCtx;
        return perform(nodes, (c, i) -> i.preMap(c, metaCtx), initCtxExec);
    }

    public DataDepCtx postMap(DataDepCtx initCtx, MetaCtx metaCtx, List<? extends GraphNode<DataDepCtx, DataDepBubble>> nodes) {
        for (var p : preMappers()) {
            initCtx = p.apply(initCtx, metaCtx);
        }
        final DataDepCtx initCtxExec = initCtx;
        return perform(nodes, (c, i) -> i.postMap(c, metaCtx), initCtxExec);
    }

    public DataDepCtx exec(DataDepCtx initCtx,
                        MetaCtx metaCtx,
                        List<? extends GraphNode<DataDepCtx, DataDepBubble>> nodes) {
        return perform(nodes, (c, i) ->  c.executableFor(i) ? i.exec(c, metaCtx) : c, initCtx);
    }

    private DataDepBubble doExec(MetaCtx metaCtx, DataDepCtx p, AtomicReference<DataDepBubble> prev) {
        return Optional.ofNullable(prev.get())
                .map(i -> this.exec(p, i, metaCtx))
                .or(() -> Optional.ofNullable(this.exec(p, metaCtx)))
                .map(i -> {
                    prev.set(i);
                    return i;
                })
                .orElse(null);
    }

    public static DataDepCtx perform(List<? extends GraphNode<DataDepCtx, DataDepBubble>> nodes,
                                  BiFunction<DataDepCtx, GraphNode<DataDepCtx, DataDepBubble>, DataDepCtx> initCtxFunction,
                                  DataDepCtx initCtx) {
        for (var n : nodes) {
            initCtx = initCtxFunction.apply(initCtx, n);
        }

        return initCtx;
    }


}
