package com.hayden.test_graph.init.graph;

import com.hayden.test_graph.graph.*;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.test_graph.graph.node.TestGraphNode;
import com.hayden.test_graph.graph.service.GraphAutoDetect;
import com.hayden.test_graph.graph.service.LazyGraphAutoDetect;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.exec.single.InitNode;
import com.hayden.test_graph.thread.ThreadScope;
import com.hayden.utilitymodule.MapFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ThreadScope
public class InitGraph implements TestGraph<InitCtx, InitBubble> {

    @Lazy
    @Autowired
    LazyGraphAutoDetect nodesProvider;
    @Autowired
    TestGraphSort graphSort;

    @Autowired
    @ThreadScope
    List<SubGraph<InitCtx, InitBubble>> subGraphs;

    Map<Class<? extends InitCtx>, List<? extends GraphNode<InitCtx, InitBubble>>> nodes = new HashMap<>();

    @ThreadScope
    @Autowired(required = false)
    public void setNodes(List<InitNode<InitCtx>> nodes) {
        this.nodes = MapFunctions.CollectMap(
                nodes.stream()
                        .collect(Collectors.groupingBy(TestGraphNode::clzz))
                        .entrySet().stream()
                        .map(e -> Map.entry(e.getKey(), graphSort.sort(e.getValue())))
        );
    }

    @Override
    public TestGraphSort sortingAlgorithm() {
        return graphSort;
    }

    @Override
    public List<? extends InitCtx> sortedCtx(Class<? extends InitCtx> init) {
        var i = graphSort.sortContext(
                subGraphs.stream()
                        .filter(s -> s.clazz().equals(init))
                        .flatMap(s -> s.parseContextTree().stream())
                        .toList()
        );
        return i;
    }

    @Override
    public Map<Class<? extends InitCtx>, List<? extends GraphNode<InitCtx, InitBubble>>> sortedNodes() {
        return this.nodes;
    }

    @Override
    public GraphAutoDetect allNodes() {
        return nodesProvider.getAutoDetect();
    }

}
