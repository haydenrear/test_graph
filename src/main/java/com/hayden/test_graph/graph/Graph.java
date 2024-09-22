package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.node.TestGraphNode;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.utilitymodule.MapFunctions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface Graph {


     static <T extends TestGraphContext<U>, U extends HyperGraphContext<MetaCtx>, N extends TestGraphNode<? extends T, ? extends U>> Map<Class<? extends T>, List<N>> collectNodes(
             List<? extends N> nodes,
             TestGraphSort graphSort
     ) {
         Map<Class<? extends N>, ? extends List<? extends N>> collect = nodes.stream()
                 .collect(Collectors.groupingBy(t -> (Class<? extends N>) t.clzz()));
         var classListMap = MapFunctions.CollectMap(
                 collect
                         .entrySet()
                         .stream()
                         .map(e -> Map.entry(e.getKey(), graphSort.sort(e.getValue())))
         );
         return MapFunctions.CollectMap(
                 classListMap
                         .entrySet()
                         .stream()
                         .map(e -> Map.entry(
                                 (Class<? extends T>) e.getKey(),
                                 javaGraphNodes(Map.entry(e.getKey(), e.getValue().stream().map(n -> (N) n).toList()))
                         ))
         );
    }

    private static <T extends TestGraphContext<U>, U extends HyperGraphContext<MetaCtx>, N extends TestGraphNode<? extends T, ? extends U>>
    @NotNull List<N> javaGraphNodes(
            Map.Entry<Class<? extends N>, List<? extends N>> e
    ) {
        return e.getValue()
                .stream()
                .map(i -> (N) i)
                .toList();
    }

}
