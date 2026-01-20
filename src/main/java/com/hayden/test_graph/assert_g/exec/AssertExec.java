package com.hayden.test_graph.assert_g.exec;

import com.hayden.test_graph.assert_g.ctx.AssertBubble;
import com.hayden.test_graph.assert_g.ctx.AssertCtx;
import com.hayden.test_graph.assert_g.graph.AssertGraph;
import com.hayden.test_graph.exec.single.GraphExec;
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
public class AssertExec implements GraphExec.ExecNode<AssertCtx, AssertBubble> {

    public interface InitReducer extends GraphExecReducer<AssertCtx, AssertBubble> {}

    public interface InitPreMapper extends GraphExecMapper<AssertCtx, AssertBubble> {}
    public interface InitPostMapper extends GraphExecMapper<AssertCtx, AssertBubble> {}

    @Autowired(required = false)
    List<InitReducer> reducers;
    @Autowired(required = false)
    List<InitPreMapper> preMappers;
    @Autowired(required = false)
    List<InitPostMapper> postMappers;

    @Autowired
    @ResettableThread
    AssertGraph initGraph;

    @Override
    public AssertBubble execInner(AssertCtx initCtx, AssertBubble prev, MetaCtx metaCtx) {
        var nodes = this.initGraph.toRunSortedNodes(initCtx);
        var toExec = retrieveToExec(initCtx, prev, metaCtx);
        initCtx = toExec.preMap(initCtx, metaCtx, nodes);
        initCtx = toExec.exec(initCtx, metaCtx, nodes);
        final AssertCtx initCtxExec = toExec.postMap(initCtx, metaCtx, nodes);
        return Optional.ofNullable(initCtxExec)
                .map(AssertCtx::bubble)
                .stream()
                .findAny()
                .orElse(null);
    }

    private AssertExec retrieveToExec(AssertCtx initCtx, AssertBubble prev, MetaCtx metaCtx) {
        return this;
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
    public List<? extends GraphExecMapper<AssertCtx, AssertBubble>> postMappers() {
        return Optional.ofNullable(postMappers).orElse(new ArrayList<>());
    }

    @Override
    public AssertBubble collectCtx(Class<? extends AssertCtx> toCollect, MetaCtx metaCtx) {
        List<? extends AssertCtx> intCtx = this.initGraph.sortedCtx(toCollect)
                .stream()
                .toList();

        if (intCtx.isEmpty()) {
            logBubbleError();
            return null;
        } else if (intCtx.size() == 1) {
            return this.exec(intCtx.getFirst(), metaCtx);
        } else {
            AtomicReference<AssertBubble> prev = new AtomicReference<>();
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

    public AssertCtx preMap(AssertCtx initCtx, MetaCtx metaCtx, List<GraphExecNode<AssertCtx>> nodes) {
        for (var p : preMappers()) {
            initCtx = p.apply(initCtx, metaCtx);
        }
        final AssertCtx initCtxExec = initCtx;
        return perform(nodes, (c, i) -> i.preMap(c, metaCtx), initCtxExec);
    }

    public AssertCtx postMap(AssertCtx initCtx, MetaCtx metaCtx, List<GraphExecNode<AssertCtx>> nodes) {
        for (var p : preMappers()) {
            initCtx = p.apply(initCtx, metaCtx);
        }
        final AssertCtx initCtxExec = initCtx;
        return perform(nodes, (c, i) -> i.postMap(c, metaCtx), initCtxExec);
    }

    public AssertCtx exec(AssertCtx initCtx,
                        MetaCtx metaCtx,
                        List<GraphExecNode<AssertCtx>> nodes) {
        return perform(nodes, (c, i) ->  c.executableFor(i) ? i.exec(c, metaCtx) : c, initCtx);
    }

    private AssertBubble doExec(MetaCtx metaCtx, AssertCtx p, AtomicReference<AssertBubble> prev) {
        return Optional.ofNullable(prev.get())
                .map(i -> this.exec(p, i, metaCtx))
                .or(() -> Optional.ofNullable(this.exec(p, metaCtx)))
                .map(i -> {
                    prev.set(i);
                    return i;
                })
                .orElse(null);
    }

    public static AssertCtx perform(List<GraphExecNode<AssertCtx>> nodes,
                                    BiFunction<AssertCtx, GraphExecNode<AssertCtx>, AssertCtx> initCtxFunction,
                                    AssertCtx initCtx) {
        for (var n : nodes) {
            initCtx = initCtxFunction.apply(initCtx, n);
        }

        return initCtx;
    }

}
