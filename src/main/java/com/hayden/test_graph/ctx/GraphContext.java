package com.hayden.test_graph.ctx;

import com.hayden.utilitymodule.sort.GraphSort;

import java.util.List;

public interface GraphContext<G extends GraphContext<G>> extends GraphSort.GraphSortable<G> {

}
