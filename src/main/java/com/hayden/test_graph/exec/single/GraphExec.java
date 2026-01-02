package com.hayden.test_graph.exec.single;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.data_dep.ctx.DataDepBubble;
import com.hayden.test_graph.data_dep.ctx.DataDepCtx;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.utilitymodule.fn.Reducer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface GraphExec<CTX extends TestGraphContext> {

    Logger log = LoggerFactory.getLogger(GraphExec.class);

    interface ExecNode<CTX extends TestGraphContext<H>, H extends HyperGraphContext> extends GraphExec<CTX> {

        default List<Class<? extends HyperGraphContext>> dependencies(Class<? extends TestGraphContext> ctx) {
            return new ArrayList<>();
        }

        default H exec(CTX initCtx, MetaCtx metaCtx) {
            return this.exec(initCtx, null, metaCtx);
        }

        default H collectCtx(Class<? extends CTX> toCollect) {
            return collectCtx(toCollect, null);
        }

        default H exec(CTX c, H prev, MetaCtx metaCtx) {
            if (metaCtx.didRun(c.getClass())) {
                return c.bubble();
            }
            metaCtx.ran(c);
            if (c.skip()) {
                log.info("Skipping exec {}", c.getClass().getName());
                return c.bubble();
            }
            return execInner(c, prev, metaCtx);
        }

        H collectCtx(Class<? extends CTX> toCollect, @Nullable MetaCtx h) ;

        H execInner(CTX c, H prev, MetaCtx metaCtx);


        default boolean skipFilter(CTX i) {
            if (i.skip()) {
                log.info("Skipping exec of {}.", i.getClass().getSimpleName());
                return false;
            }

            return true;
        }


    }


    interface GraphExecNode<CTX extends TestGraphContext> extends GraphExec<CTX> {

        default boolean skip(CTX t) {
            return false;
        }

        default CTX preMap(CTX c, MetaCtx h) {
            return c;
        }

        default CTX postMap(CTX c, MetaCtx h) {
            return c;
        }

        default CTX exec(CTX c, MetaCtx h) {
            return c;
        }

        default CTX exec(CTX c, HyperGraphContext hgCtx, MetaCtx h) {
            return exec(c, h);
        }

    }

    /**
     * To bubbles
     * @param <CTX>
     * @param <H>
     */
    interface GraphExecReducer<CTX extends TestGraphContext, H extends HyperGraphContext> extends BiFunction<CTX, H, H> {

        H reduce(CTX first, H second);

        @Override
        default H apply(CTX ctx, H h) {
            return reduce(ctx, h);
        }
    }

    interface GraphExecMapper<CTX extends TestGraphContext, H extends HyperGraphContext> extends BiFunction<CTX, MetaCtx, CTX> {

        CTX reduce(CTX first, MetaCtx h);

        @Override
        default CTX apply(CTX ctx, MetaCtx h) {
            return reduce(ctx, h);
        }
    }

    default <H extends HyperGraphContext> List<? extends GraphExecReducer<CTX, H>> reducers() {
        return new ArrayList<>() ;
    }

    default <H extends HyperGraphContext> List<? extends GraphExecMapper<CTX, H>> preMappers() {
        return new ArrayList<>();
    }

    default <H extends HyperGraphContext> List<? extends GraphExecMapper<CTX, H>> postMappers() {
        return new ArrayList<>();
    }

    static <T, U> Optional<T> chainCtx(List<? extends BiFunction<U, T, T>> reducers,
                                       List<? extends U> ctx,
                                       Function<U, T> extract) {
        return Reducer.chainReducers(reducers.subList(0, reducers.size()))
                .flatMap(red -> doReducer(ctx, extract, red))
                .or(() -> {
                    if (reducers.isEmpty()) {
                        log.warn("Did not find any reducers. Returning first reducer as measured by sort.");
                    }

                    return doReducer(ctx, extract, (c, e) -> e);
                });
    }

    private static <T, U> @NotNull Optional<T> doReducer(List<? extends U> ctx,
                                                         Function<U, T> extract,
                                                         BiFunction<U, T, T> red) {
        return ctx.stream()
                .map(i -> {
                    T apply = extract.apply(i);
                    return Map.entry(apply, i);
                })
                .reduce((k1, k2) -> {
                    var re = red.apply(k1.getValue(), k1.getKey());
                    return Map.entry(re, k1.getValue());
                })
                .map(Map.Entry::getKey);
    }

    default void logBubbleError() {
        log.error("Did not find any {} context to bubble and no default provided.", this.getClass().getSimpleName());
    }
}
