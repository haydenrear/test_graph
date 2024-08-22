package com.hayden.test_graph.graph;


import com.hayden.utilitymodule.sort.GraphSort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestGraphSort {

    public Graph sort(Graph toSort) {
        return toSort.fromSorted(
                GraphSort.sort(
                        toSort.sortedNodes().stream()
                                .flatMap(t -> toGraphSortable(((TestGraphNode)t).dependsOn()).stream())
                                .toList()
                )
        );
    }


    public <T extends TestGraphNode> List<T> toGraphSortable(List<T> toSort) {
        return toSort.stream().toList();
    }

}
