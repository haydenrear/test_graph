package com.hayden.test_graph.data_dep.graph;

import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.data_dep.ctx.DataDepCtx;
import com.hayden.test_graph.data_dep.exec.single.DataDepNode;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.Graph;
import com.hayden.test_graph.graph.SubGraph;
import com.hayden.test_graph.graph.TestGraph;
import com.hayden.test_graph.meta.LazyMetaGraphDelegate;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ResettableThread
public class DataDepGraph implements TestGraph<DataDepCtx, DataDepBubble> {

    @Lazy
    @Autowired
    LazyMetaGraphDelegate nodesProvider;
    @Autowired
    TestGraphSort graphSort;

    @Autowired
    @ResettableThread
    List<SubGraph<DataDepCtx, DataDepBubble>> subGraphs;

    Map<Class<? extends DataDepCtx>, List<GraphExec.GraphExecNode<DataDepCtx>>> nodes = new HashMap<>();

    @ResettableThread
    @Autowired(required = false)
    public void setNodes(List<? extends DataDepNode> nodes) {
        this.nodes = Graph.collectNodes(nodes, graphSort);
    }

    @Override
    public List<? extends DataDepCtx> sortedCtx(Class<? extends DataDepCtx> init) {
        return graphSort.sortContext(
                        subGraphs.stream()
                                .filter(s -> s.clazz().equals(init))
                                .flatMap(s -> s.parseContextTree().stream())
                                .toList()
                )
                .reversed();
    }

    @Override
    public Map<Class<? extends DataDepCtx>, List<GraphExec.GraphExecNode<DataDepCtx>>> sortedNodes() {
        return this.nodes;
    }


}
