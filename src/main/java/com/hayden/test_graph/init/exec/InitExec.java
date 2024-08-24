package com.hayden.test_graph.init.exec;

import com.hayden.test_graph.edge.GraphEdges;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.GraphNode;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.graph.InitGraph;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ThreadScope;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ThreadScope
public class InitExec implements GraphExec.ExecNode<InitCtx, InitBubble> {

    public interface InitReducer extends GraphExecReducer<InitCtx, InitBubble> {}

    public interface InitPreMapper extends GraphExecMapper<InitCtx, InitBubble> {}

    public interface InitPostMapper extends GraphExecMapper<InitCtx, InitBubble> {}

    @Autowired(required = false)
    List<InitReducer> reducers;
    @Autowired(required = false)
    List<InitPreMapper> preMappers;
    @Autowired(required = false)
    List<InitPostMapper> postMappers;

    @Autowired
    GraphEdges graphEdges;

    @Autowired
    @ThreadScope
    InitGraph initGraph;

    public InitCtx preMap(InitCtx initCtx, MetaCtx metaCtx, List<? extends GraphNode<InitCtx, InitBubble>> nodes) {
        return nodes.stream()
                .map(gn -> gn.preMap(initCtx, metaCtx))
                .toList()
                .getLast();
    }

    public InitCtx postMap(InitCtx initCtx, MetaCtx metaCtx, List<? extends GraphNode<InitCtx, InitBubble>> nodes) {
        return nodes.stream()
                .map(gn -> gn.postMap(initCtx, metaCtx))
                .toList()
                .getLast();
    }

    public InitCtx exec(InitCtx initCtx, MetaCtx metaCtx, List<? extends GraphNode<InitCtx, InitBubble>> nodes) {
        return nodes.stream()
                .map(gn -> gn.exec(initCtx, metaCtx))
                .toList()
                .getLast();
    }

    public InitBubble exec(InitCtx initCtx, MetaCtx metaCtx) {
        var nodes = this.initGraph.sortedNodes().get(initCtx.getClass());
        initCtx = preMap(initCtx, metaCtx, nodes);
        initCtx = exec(initCtx, metaCtx, nodes);
        final InitCtx initCtxExec = postMap(initCtx, metaCtx, nodes);
        return Optional.ofNullable(initCtx)
                .map(InitCtx::bubble)
                .stream()
                .peek(i -> graphEdges.addEdge(this, initCtxExec, i, metaCtx))
                .findAny()
                .orElse(null);
    }

    @Override
    public List<InitReducer> reducers() {
        return Optional.ofNullable(reducers).orElse(new ArrayList<>());
    }

    @Override
    public List<InitPreMapper> preMappers() {
        return Optional.ofNullable(preMappers).orElse(new ArrayList<>());
    }

    @Override
    public List<? extends GraphExecMapper<InitCtx, InitBubble>> postMappers() {
        return Optional.ofNullable(postMappers).orElse(new ArrayList<>());
    }

    @Override
    public InitBubble collectCtx(Class<? extends InitCtx> toCollect, MetaCtx metaCtx) {
        List<? extends InitCtx> intCtx = this.initGraph.sortedCtx(toCollect);
        if (intCtx.isEmpty()) {
            logBubbleError();
            return null;
        } else if (intCtx.size() == 1) {
            return intCtx.getFirst().bubble();
        } else {
            return GraphExec.chainCtx(this.reducers(), intCtx, p -> this.exec(p, metaCtx))
                    .orElseGet(() -> {
                        logBubbleError();
                        return null;
                    });
        }
    }

}
