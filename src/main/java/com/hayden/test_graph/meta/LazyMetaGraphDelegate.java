package com.hayden.test_graph.meta;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.graph.Graph;
import com.hayden.test_graph.graph.node.TestGraphNode;
import com.hayden.test_graph.meta.graph.MetaGraph;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.sort.GraphSort;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Component
public class LazyMetaGraphDelegate {

    @Lazy
    @ResettableThread
    @Autowired
    MetaGraphDelegate autoDetect;

    @Autowired
    @ResettableThread
    Assertions assertions;

    @ResettableThread
    @Autowired
    public void setMetaGraph(MetaGraph metaGraph) {
        autoDetect.setMetaGraph(metaGraph);
    }

    public List<Class<? extends TestGraphContext>> sort(List<Class<? extends TestGraphContext>> toSort) {
        var all = toSort.stream().map(autoDetect::getGraphContext)
                .flatMap(Optional::stream)
                .toList();

        var sorted = GraphSort.sort(all);

        assertions.assertSoftly(sorted.size() == toSort.size(), "Size of returned graphs not consistent.");
        assertions.assertSoftly(sorted.stream().allMatch(s -> toSort.contains(s.getClass())), "Sorted did not contain some.");

        List<Class<? extends TestGraphContext>> classes = sorted.stream()
                .map(tgc -> (Class<? extends TestGraphContext>) tgc.getClass())
                .collect(Collectors.toCollection(() -> {
                    List<Class<? extends TestGraphContext>> c = new ArrayList<>();
                    return c;
                }));

        return classes;

    }

    public <T extends TestGraphContext> Optional<T> getGraphContext(Class<T> clazz) {
        return autoDetect.getGraphContext(clazz);
    }

    public void setGraphs(List<Graph> graphNodes) {
        autoDetect.setGraphs(graphNodes);
    }

    public void setNodes(List<TestGraphNode> graphNodes) {
        autoDetect.setNodes(graphNodes);
    }

    public void setGraphContext(List<TestGraphContext> graphCtx) {
        autoDetect.setGraphContext(graphCtx);
    }

    public List<HyperGraphExec> parseHyperGraph(Class<? extends TestGraphContext> clazz) {
        return autoDetect.parseHyperGraph(clazz);
    }

    public List<HyperGraphExec> retrieve(HyperGraphExec hyperGraphExec) {
        return autoDetect.retrieve(hyperGraphExec);
    }

    public Stream<Class<? extends TestGraphContext>> parseSubGraph(HyperGraphExec<TestGraphContext<HyperGraphContext>, HyperGraphContext> hg, Class<? extends TestGraphContext> clazz) {
        return autoDetect.parseSubGraph(hg, clazz);
    }

    public HyperGraphExec getMatchingContext(Class<? extends TestGraphContext> clazz) {
        return autoDetect.getMatchingContext(clazz);
    }
}
