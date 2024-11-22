package com.hayden.test_graph.meta.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.graph.node.HyperGraphBubbleNode;
import com.hayden.test_graph.meta.exec.prog_bubble.MetaProgNode;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Stream;

@Slf4j
@Component
@ResettableThread
public class MetaProgCtx implements MetaCtx {

    @Delegate
    Stack<HyperGraphContext> delegates = new Stack<>();

    public <T extends HyperGraphContext> Stream<T> retrieveBubbled(Class<T> clazz) {
        return delegates.stream()
                .map(HyperGraphContext::bubble)
                .filter(Objects::nonNull)
                .distinct()
                .filter(b -> b.getClass().equals(clazz))
                .flatMap(hgc -> {
                    try {
                        return Stream.of((T) hgc) ;
                    } catch (ClassCastException c) {
                        log.error("{}, {}", c.getMessage(), c.getStackTrace());
                        return Stream.empty();
                    }
                });
    }

    public <T extends MetaCtx> Stream<T> retrieve(Class<T> clazz) {
        return delegates.stream()
                .filter(Objects::nonNull)
                .filter(m -> clazz.equals(m.getClass()))
                .flatMap(m -> {
                    try {
                        return Stream.of((T) m);
                    } catch (ClassCastException e) {
                        return Stream.empty();
                    }
                });
    }

    @Override
    public HyperGraphContext<MetaCtx> getBubbled() {
        return this;
    }

    @Override
    public MetaCtx bubbleMeta(MetaCtx metaCtx) {
        return this;
    }

    @Override
    public MetaCtx bubble() {
        return this;
    }

    @Override
    public Class<? extends MetaCtx> bubbleClazz() {
        return MetaProgCtx.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof HyperGraphBubbleNode<?>;
    }

    @Override
    public boolean executableFor(MetaProgNode n) {
        return n != null;
    }

    @Override
    public List<Class<? extends TestGraphContext<MetaCtx>>> dependsOn() {
        return List.of();
    }

    @Override
    public boolean toSet(TestGraphContext context) {
        return false;
    }

    @Override
    public void doSet(TestGraphContext context) {

    }

    @Override
    public boolean isLeafNode() {
        return false;
    }

    @Override
    public ContextValue<TestGraphContext> child() {
        return ContextValue.empty();
    }

    @Override
    public ContextValue<TestGraphContext> parent() {
        return ContextValue.empty();
    }

    @Override
    public Stack<? extends HyperGraphContext> prev() {
        return delegates;
    }
}
