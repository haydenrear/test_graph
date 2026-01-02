package com.hayden.test_graph.graph.service;


import com.google.common.collect.Lists;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.node.GraphNode;
import com.hayden.utilitymodule.MapFunctions;
import com.hayden.utilitymodule.sort.GraphSort;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.util.ProxyUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TestGraphSort {

    public <G extends GraphNode> List<G> sort(List<G> toSort) {
        List<G> toSortWithDeps = doSort(toSort, this::toGraphSortable);
        return GraphSort.sort(toSortWithDeps.stream().distinct().toList());
    }

    public <G extends TestGraphContext> List<G> sortContext(List<G> toSort) {
        List<G> sortWithDeps = doSort(toSort, this::toGraphSortable);
        return GraphSort.sort(sortWithDeps);
    }

    public <T extends GraphSort.GraphSortable> List<T> toGraphSortable(List<T> toSort) {
        List<T> out = new ArrayList<>();
        var values = MapFunctions.CollectMap(toSort.stream().filter(Objects::nonNull).map(e -> {
            System.out.println(toSort);
            return Map.entry(ProxyUtils.getUserClass(e.getClass()), e);
        }));
        for (var t : toSort) {
            List dependsOnValues = t.dependsOn();

            if (!dependsOnValues.isEmpty()) {
                var v = getVals(dependsOnValues.stream().map(values::get).toList());
                List list = dependsOnValues.stream().map(v::get).filter(Objects::nonNull).toList();
                out.addAll(toGraphSortable(list));
            }

            out.add(t);
        }
        return out;
    }

    private <G extends GraphSort.GraphSortable> @NotNull List<G> doSort(List<G> toSort, Function<List<G>, List<G>> fn) {
        var vals = getVals(toSort);
        return toSort.stream()
                .flatMap(t -> {
                    System.out.println(toSort);
                    List<G> outList = Lists.newArrayList(t) ;
                    List<G> list = (List<G>) t.dependsOn().stream().map(vals::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toCollection(() -> outList));
                    return fn.apply(list).stream();
                })
                .toList();
    }

    private static <G extends GraphSort.GraphSortable> Map<? extends Class<? extends GraphSort.GraphSortable>, G> getVals(List<G> toSort) {
        return MapFunctions.CollectMap(
                toSort.stream()
                        .filter(Objects::nonNull)
                .map(e -> Map.entry(
                        (Class<? extends GraphSort.GraphSortable>) ProxyUtils.getUserClass(e.getClass()), e)));
    }

}
