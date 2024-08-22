package com.hayden.test_graph.init.graph;

import com.hayden.test_graph.ctx.HierarchicalContext;
import com.hayden.test_graph.graph.*;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.exec.single.InitNode;
import com.hayden.test_graph.thread.ThreadScope;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ThreadScope
public class InitGraph<I extends InitCtx> implements TestGraph<I, InitBubble, InitNode<I>> {

    @Autowired
    GraphAutoDetect nodesProvider;

    List<? extends InitCtx> ctx;

    @PostConstruct
    public void initialize() {
        this.ctx = nodesProvider.retrieveCtx(t -> t instanceof InitCtx c ? c : null);
    }

    public void setParentChildren() {
        for (var i : ctx) {
            for (var j : ctx) {
                if (i != j) {
                    if (i instanceof HierarchicalContext.HasChildContext c
                            && j instanceof HierarchicalContext.HasParentContext p) {
                        if (p.toSet(c)) {
                            p.doSet(c);
                        }
                    } else if (j instanceof HierarchicalContext.HasChildContext c
                            && i instanceof HierarchicalContext.HasParentContext p) {
                        if (p.toSet(c)) {
                            p.doSet(c);
                        }
                    }
                }
            }
        }
    }


    @Override
    public InitGraph<I> fromSorted(List<InitNode<I>> nodes) {
        return null;
    }

    @Override
    public List<? extends TestGraphNode> sortedNodes() {
        return List.of();
    }

    @Override
    public InitBubble bubble() {
        return null;
    }

    @Override
    public I ctx() {
        return null;
    }

    @Override
    public void initialize(HyperGraph hg,
                           InitBubble hyperGraphContext,
                           HyperGraphNode hgn) {
    }

    @Override
    public GraphAutoDetect allNodes() {
        return nodesProvider;
    }
}
