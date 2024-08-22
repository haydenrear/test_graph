package com.hayden.test_graph.init.graph;

import com.hayden.test_graph.ctx.HierarchicalContext;
import com.hayden.test_graph.graph.*;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.exec.single.InitNode;
import com.hayden.test_graph.thread.ThreadScope;
import com.hayden.utilitymodule.MapFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ThreadScope
public class InitGraph implements TestGraph<InitCtx, InitBubble> {

    @Autowired
    @Lazy
    LazyGraphAutoDetect nodesProvider;
    @Autowired
    TestGraphSort graphSort;

    Map<Class<? extends InitCtx>, List<? extends GraphNode<InitCtx, InitBubble>>> nodes;

    @Autowired
    @ThreadScope
    List<SubGraph<InitCtx, InitBubble>> subGraphs;

    public void initialize() {
//        this.setParentChildren();
    }

    @Autowired(required = false)
    @ThreadScope
    public void setNodes(List<InitNode<InitCtx>> nodes) {
        this.nodes = MapFunctions.CollectMap(
                nodes.stream().collect(Collectors.groupingBy(TestGraphNode::clzz))
                        .entrySet().stream().map(e -> Map.entry(e.getKey(), graphSort.sort(e.getValue())))
        );
    }

    @Override
    public TestGraphSort sortingAlgorithm() {
        return graphSort;
    }

    @Override
    public List<? extends InitCtx> sortedCtx(Class<? extends InitCtx> init) {
        return graphSort.sortContext(
                subGraphs.stream()
                        .filter(s -> s.clazz().equals(init))
                        .flatMap(s -> s.parseContextTree().stream())
                        .toList()
        );
    }

    @Override
    public Map<Class<? extends InitCtx>, List<? extends GraphNode<InitCtx, InitBubble>>> sortedNodes() {
        return this.nodes;
    }

    @Override
    public List<SubGraph<InitCtx, InitBubble>> subGraphs() {
        return subGraphs;
    }

    @Override
    public GraphAutoDetect allNodes() {
        return nodesProvider.getAutoDetect();
    }

//    public void setParentChildren() {
//        for (var i : ctx) {
//            for (var j : ctx) {
//                if (i != j) {
//                    if (i instanceof HierarchicalContext.HasChildContext c
//                            && j instanceof HierarchicalContext.HasParentContext p) {
//                        if (p.toSet(c)) {
//                            p.doSet(c);
//                        }
//                    } else if (j instanceof HierarchicalContext.HasChildContext c
//                            && i instanceof HierarchicalContext.HasParentContext p) {
//                        if (p.toSet(c)) {
//                            p.doSet(c);
//                        }
//                    }
//                }
//            }
//        }
//    }
}
