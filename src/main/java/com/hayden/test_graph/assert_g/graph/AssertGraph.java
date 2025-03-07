package com.hayden.test_graph.assert_g.graph;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.assert_g.ctx.AssertCtx;
import com.hayden.test_graph.assert_g.exec.single.AssertNode;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.Graph;
import com.hayden.test_graph.graph.SubGraph;
import com.hayden.test_graph.graph.TestGraph;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.meta.LazyMetaGraphDelegate;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ResettableThread
public class AssertGraph implements TestGraph<AssertCtx, AssertBubble> {

    @Lazy
    @Autowired
    LazyMetaGraphDelegate nodesProvider;
    @Autowired
    TestGraphSort graphSort;

    @Autowired
    @ResettableThread
    List<SubGraph<AssertCtx, AssertBubble>> subGraphs;

    Map<Class<? extends AssertCtx>, List<GraphExec.GraphExecNode<AssertCtx>>> nodes = new HashMap<>();

    @ResettableThread
    @Autowired(required = false)
    public void setNodes(List<? extends AssertNode> nodes) {
        this.nodes = Graph.collectNodes(nodes, graphSort);
    }

    @Override
    public List<? extends AssertCtx> sortedCtx(Class<? extends AssertCtx> init) {
        return graphSort.sortContext(
                        subGraphs.stream()
                                .filter(s -> s.clazz().equals(init))
                                .flatMap(s -> s.parseContextTree().stream())
                                .toList()
                )
                .reversed();
    }

    @Override
    public Map<Class<? extends AssertCtx>, List<GraphExec.GraphExecNode<AssertCtx>>> sortedNodes() {
        return this.nodes;
    }


}
