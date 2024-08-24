package com.hayden.test_graph.exec.single;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
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

public interface GraphExec<CTX extends TestGraphContext<H>, H extends HyperGraphContext> {

    Logger log = LoggerFactory.getLogger(GraphExec.class);

    interface ExecNode<CTX extends TestGraphContext<H>, H extends HyperGraphContext> extends GraphExec<CTX, H> {

        H collectCtx(Class<? extends CTX> toCollect, @Nullable MetaCtx h) ;

        default H collectCtx(Class<? extends CTX> toCollect) {
            return collectCtx(toCollect, null);
        }

        H exec(CTX c, MetaCtx metaCtx);

        H exec(CTX c, H prev, MetaCtx metaCtx);

    }

    interface GraphExecNode<CTX extends TestGraphContext<H>, H extends HyperGraphContext> extends GraphExec<CTX, H> {

        default CTX preMap(CTX c, MetaCtx h) {
            return c;
        }

        default CTX postMap(CTX c, MetaCtx h) {
            return c;
        }

        default CTX exec(CTX c, MetaCtx h) {
            return c;
        }

        default CTX exec(CTX c, H hgCtx, MetaCtx h) {
            return exec(c, h);
        }

    }

    /**
     * To bubbles
     * @param <CTX>
     * @param <H>
     */
    interface GraphExecReducer<CTX extends TestGraphContext<H>, H extends HyperGraphContext> extends BiFunction<CTX, H, H> {

        H reduce(CTX first, H second);

        @Override
        default H apply(CTX ctx, H h) {
            return reduce(ctx, h);
        }
    }

    interface GraphExecMapper<CTX extends TestGraphContext<H>, H extends HyperGraphContext> extends BiFunction<CTX, MetaCtx, CTX> {

        CTX reduce(CTX first, MetaCtx h);

        @Override
        default CTX apply(CTX ctx, MetaCtx h) {
            return reduce(ctx, h);
        }
    }

    default List<? extends GraphExecReducer<CTX, H>> reducers() {
        return new ArrayList<>() ;
    }

    default List<? extends GraphExecMapper<CTX, H>> preMappers() {
        return new ArrayList<>();
    }

    default List<? extends GraphExecMapper<CTX, H>> postMappers() {
        return new ArrayList<>();
    }

    static <T, U> Optional<T> chainCtx(List<? extends BiFunction<U, T, T>> reducers,
                                       List<? extends U> ctx,
                                       Function<U, T> extract) {
        return Reducer.chainReducers(reducers.subList(0, reducers.size()))
                .flatMap(red -> doReducer(ctx, extract, red))
                .or(() -> {
                    if (ctx.size() > 1) {
                        log.warn("Did not find any reducers. Returning first reducer as measured by sort.");
                    }
                    return ctx.stream().findAny().map(extract);
                });
    }

    private static <T, U> @NotNull Optional<T> doReducer(List<? extends U> ctx,
                                                         Function<U, T> extract,
                                                         BiFunction<U, T, T> red) {
        return ctx.stream()
                .map(i -> Map.entry(extract.apply(i), i))
                .reduce((k1, k2) -> {
                    var re = red.apply(k1.getValue(), extract.apply(k2.getValue()));
                    return Map.entry(re, k1.getValue());
                })
                .map(Map.Entry::getKey);
    }

    default void logBubbleError() {
        log.error("Did not find any {} context to bubble and no default provided.", this.getClass().getSimpleName());
    }
}
