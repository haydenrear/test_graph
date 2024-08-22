package com.hayden.test_graph.init.exec;

import com.hayden.test_graph.edge.GraphEdges;
import com.hayden.test_graph.exec.single.GraphExec;
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

    public interface InitMapper extends GraphExecMapper<InitCtx, InitBubble> {}

    @Autowired(required = false)
    List<InitReducer> reducers;
    @Autowired(required = false)
    List<InitMapper> mappers;
    @Autowired
    @ThreadScope
    InitGraph initGraph;
    @Autowired
    GraphEdges graphEdges;

    public InitBubble exec(InitCtx initCtx, MetaCtx metaCtx) {
        return Optional.ofNullable(
                        this.initGraph.sortedNodes()
                                .get(initCtx.getClass())
                                .stream()
                                .filter(initCtx::executableFor)
                                .map(i -> i.exec(initCtx))
                                .toList()
                                .getLast()
                )
                .map(ic -> {
                    for (var m : mappers())
                        ic = m.apply(ic);

                    return ic;
                })
                .map(InitCtx::bubble)
                .stream()
                .peek(i -> graphEdges.addEdge(this, initCtx, i, metaCtx))
                .findAny()
                .orElse(null);
    }

    @Override
    public List<InitReducer> reducers() {
        return Optional.ofNullable(reducers).orElse(new ArrayList<>());
    }

    @Override
    public List<InitMapper> mappers() {
        return Optional.ofNullable(mappers).orElse(new ArrayList<>());
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
