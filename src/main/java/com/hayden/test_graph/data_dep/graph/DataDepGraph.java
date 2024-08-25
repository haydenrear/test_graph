package com.hayden.test_graph.data_dep.graph;

import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.data_dep.ctx.DataDepCtx;
import com.hayden.test_graph.data_dep.exec.single.DataDepNode;
import com.hayden.test_graph.graph.Graph;
import com.hayden.test_graph.graph.SubGraph;
import com.hayden.test_graph.graph.TestGraph;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.meta.LazyMetaGraphDelegate;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.thread.ThreadScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ThreadScope
public class DataDepGraph implements TestGraph<DataDepCtx, DataDepBubble> {

    @Lazy
    @Autowired
    LazyMetaGraphDelegate nodesProvider;
    @Autowired
    TestGraphSort graphSort;

    @Autowired
    @ThreadScope
    List<SubGraph<DataDepCtx, DataDepBubble>> subGraphs;

    Map<Class<? extends DataDepCtx>, List<GraphNode<DataDepCtx, DataDepBubble>>> nodes = new HashMap<>();

    @ThreadScope
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
        );
    }

    @Override
    public Map<Class<? extends DataDepCtx>, List<GraphNode<DataDepCtx, DataDepBubble>>> sortedNodes() {
        return this.nodes;
    }


}
