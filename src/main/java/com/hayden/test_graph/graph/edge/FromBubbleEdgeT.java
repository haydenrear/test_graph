package com.hayden.test_graph.graph.edge;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.meta.ctx.MetaProgCtx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public interface FromBubbleEdgeT<T extends TestGraphContext<H>, H extends HyperGraphContext<H>, U extends HyperGraphContext>
        extends PreExecTestGraphEdge<T, H>{

    Logger log = LoggerFactory.getLogger(FromBubbleEdgeT.class);

    Class<U> bubbleClazz();

    Class<T> initClazz();

    void set(T setOn, U setFrom);

    @Override
    default T edge(T transform, MetaCtx s) {
        if (s instanceof MetaProgCtx second) {
            var i = second.retrieveBubbled(bubbleClazz())
                    .toList();

            if (i.size() != 1)  {
                throw new RuntimeException("Failed to find commit diff init bubble: %s.".formatted(i));
            }

            i.stream().findAny()
                    .ifPresentOrElse(c -> this.set(transform, c),
                    () -> log.warn("Did not find any source of commit diff context value."));
        }
        return transform;
    }

    @Override
    default Predicate<? super Object> from() {
        return o -> initClazz().isAssignableFrom(o.getClass());
    }

    @Override
    default Predicate<? super Object> to() {
        return c -> c instanceof MetaProgCtx;
    }
}
