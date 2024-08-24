package com.hayden.test_graph.graph;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hayden.test_graph.ctx.GraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.utilitymodule.MapFunctions;
import com.hayden.utilitymodule.sort.GraphSort;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TestGraphSort {

    public <G extends GraphNode> List<G> sort(List<G> toSort) {
        List<G> toSortWithDeps = doSort(toSort, this::toGraphSortable);
        return GraphSort.sort(toSortWithDeps);
    }

    public <G extends TestGraphContext> List<G> sortContext(List<G> toSort) {
        List<G> sortWithDeps = doSort(toSort, this::toGraphSortable);
        return GraphSort.sort(sortWithDeps);
    }

    public <T extends GraphSort.GraphSortable> List<T> toGraphSortable(List<T> toSort, Set<Class<?>> prev) {
        List<T> out = new ArrayList<>();
        for (var t : toSort) {
            if (!t.dependsOn().isEmpty()) {
                var v = getVals(t.dependsOn());
                out.addAll(toGraphSortable(t.dependsOn().stream().map(l -> v.get(l.getClass())).toList(), prev));
            }

            out.add(t);
        }
        return out.stream()
                .peek(s -> peekAdd(prev, s))
                .toList();
    }

    private static <T> void peekAdd(Set<Class<?>> prev, T s) {
        if (prev.contains(s.getClass())) {
            throw new RuntimeException("Found cycle!");
        }

        prev.add(s.getClass());
    }

    private <G extends GraphSort.GraphSortable> @NotNull List<G> doSort(List<G> toSort, BiFunction<List<G>, Set<Class<?>>, List<G>> fn) {
        var vals = getVals(toSort);
        return toSort.stream()
                .flatMap(t -> {
                    List<G> outList = Lists.newArrayList(t) ;
                    List<G> list = (List<G>) t.dependsOn().stream().map(l -> vals.get(l.getClass())).collect(Collectors.toCollection(() -> outList));
                    return fn.apply(list, new HashSet<>()).stream();
                })
                .toList();
    }

    private static <G extends GraphSort.GraphSortable> Map<? extends Class<? extends GraphSort.GraphSortable>, G> getVals(List<G> toSort) {
        return MapFunctions.CollectMap(toSort.stream().map(e -> Map.entry(e.getClass(), e)));
    }

}
