package com.hayden.test_graph.init.exec;

import com.hayden.test_graph.graph.edge.EdgeExec;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.graph.InitGraph;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
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
@ResettableThread
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
    EdgeExec edgeExec;

    @Autowired
    @ResettableThread
    InitGraph initGraph;

    @Override
    public InitBubble exec(InitCtx initCtx, InitBubble prev, MetaCtx metaCtx) {
        var nodes = Optional.ofNullable(this.initGraph.sortedNodes().get(initCtx.getClass()))
                .orElse(new ArrayList<>());
        var toExec = retrieveToExec(initCtx, prev, metaCtx);
        initCtx = toExec.preMap(initCtx, metaCtx, nodes);
        initCtx = toExec.exec(initCtx, metaCtx, nodes);
        final InitCtx initCtxExec = toExec.postMap(initCtx, metaCtx, nodes);
        return Optional.ofNullable(initCtxExec)
                .map(InitCtx::bubble)
                .stream()
                .findAny()
                .orElse(null);
    }

    private InitExec retrieveToExec(InitCtx initCtx, InitBubble prev, MetaCtx metaCtx) {
        return Optional.ofNullable(prev)
                .map(ib -> edgeExec.edges(this, initCtx, ib, metaCtx))
                .orElseGet(() -> edgeExec.edges(this, initCtx, metaCtx));
    }

    @Override
    public InitBubble exec(InitCtx initCtx, MetaCtx metaCtx) {
        return exec(initCtx, null, metaCtx);
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
            return this.exec(intCtx.getFirst(), metaCtx);
        } else {
            AtomicReference<InitBubble> prev = new AtomicReference<>();
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

    public InitCtx preMap(InitCtx initCtx, MetaCtx metaCtx, List<GraphExecNode<InitCtx, InitBubble>> nodes) {
        for (var p : preMappers()) {
            initCtx = p.apply(initCtx, metaCtx);
        }
        final InitCtx initCtxExec = initCtx;
        return perform(nodes, (c, i) -> i.preMap(c, metaCtx), initCtxExec);
    }

    public InitCtx postMap(InitCtx initCtx, MetaCtx metaCtx, List<GraphExecNode<InitCtx, InitBubble>> nodes) {
        for (var p : preMappers()) {
            initCtx = p.apply(initCtx, metaCtx);
        }
        final InitCtx initCtxExec = initCtx;
        return perform(nodes, (c, i) -> i.postMap(c, metaCtx), initCtxExec);
    }

    public InitCtx exec(InitCtx initCtx,
                        MetaCtx metaCtx,
                        List<GraphExecNode<InitCtx, InitBubble>> nodes) {
        return perform(nodes, (c, i) ->  c.executableFor(i) ? i.exec(c, metaCtx) : c, initCtx);
    }

    private InitBubble doExec(MetaCtx metaCtx, InitCtx p, AtomicReference<InitBubble> prev) {
        return Optional.ofNullable(prev.get())
                .map(i -> this.exec(p, i, metaCtx))
                .or(() -> Optional.ofNullable(this.exec(p, metaCtx)))
                .map(i -> {
                    prev.set(i);
                    return i;
                })
                .orElse(null);
    }

    public static InitCtx perform(List<GraphExecNode<InitCtx, InitBubble>> nodes,
                                  BiFunction<InitCtx, GraphExecNode<InitCtx, InitBubble>, InitCtx> initCtxFunction,
                                  InitCtx initCtx) {
        for (var n : nodes) {
            initCtx = initCtxFunction.apply(initCtx, n);
        }

        return initCtx;
    }

}
