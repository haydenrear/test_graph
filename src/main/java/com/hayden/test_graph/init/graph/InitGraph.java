package com.hayden.test_graph.init.graph;

import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.*;
import com.hayden.test_graph.meta.LazyMetaGraphDelegate;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.exec.single.InitNode;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ResettableThread
public class InitGraph implements TestGraph<InitCtx, InitBubble> {

    @Autowired
    TestGraphSort graphSort;

    @Autowired
    @ResettableThread
    List<SubGraph<InitCtx, InitBubble>> subGraphs;

    Map<Class<? extends InitCtx>, List<GraphExec.GraphExecNode<InitCtx>>> nodes = new HashMap<>();

    @ResettableThread
    @Autowired(required = false)
    public void setNodes(List<? extends InitNode> nodes) {
        this.nodes = Graph.collectNodes(nodes, graphSort);
    }

    @Override
    public List<? extends InitCtx> sortedCtx(Class<? extends InitCtx> init) {
        return graphSort.sortContext(
                        subGraphs.stream()
                                .filter(s -> s.clazz().equals(init))
                                .flatMap(s -> s.parseContextTree().stream())
                                .toList()
                )
                .reversed();
    }

    @Override
    public Map<Class<? extends InitCtx>, List<GraphExec.GraphExecNode<InitCtx>>> sortedNodes() {
        return this.nodes;
    }


}
