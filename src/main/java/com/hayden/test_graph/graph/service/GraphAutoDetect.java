package com.hayden.test_graph.graph.service;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.graph.Graph;
import com.hayden.test_graph.graph.SubGraph;
import com.hayden.test_graph.graph.node.TestGraphNode;
import com.hayden.test_graph.meta.graph.MetaGraph;
import com.hayden.test_graph.thread.ThreadScope;
import com.hayden.utilitymodule.MapFunctions;
import com.hayden.utilitymodule.proxies.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@ThreadScope
public class GraphAutoDetect {

    private Map<Class<? extends TestGraphNode>, TestGraphNode> graphNodes;

    private Map<Class<? extends TestGraphContext>, TestGraphContext> graphCtxt;

    private Map<Class<? extends Graph>, Graph> graphs;

    @Autowired
    @ThreadScope
    private List<? extends SubGraph> subGraphs;

    @Autowired
    @ThreadScope
    private MetaGraph metaGraph;

    @Autowired
    private TestGraphSort graphSort;

    @Autowired
    @ThreadScope
    @Lazy
    public void setGraphs(List<Graph> graphNodes) {
        initializeMapNotProxy(graphNodes, c -> this.graphs = c);
    }

    @Autowired
    @ThreadScope
    @Lazy
    public void setNodes(List<TestGraphNode> graphNodes) {
        initializeMapNotProxy(graphNodes, c -> this.graphNodes = c);
    }

    @Autowired
    @ThreadScope
    @Lazy
    public void setGraphContext(List<TestGraphContext> graphCtx) {
        initializeMapNotProxy(graphCtx, c -> this.graphCtxt = c);
    }

    public List<HyperGraphExec> retrieveHyperGraphDependencyGraph(Class<? extends TestGraphContext> clazz) {
        return retrieve(getMatchingContext(clazz));
    }

    public List<HyperGraphExec> retrieve(HyperGraphExec hyperGraphExec) {
        return graphSort.sort(retrieve(hyperGraphExec, new HashSet<>(), retrieveHg()));
    }

    public List<HyperGraphExec> retrieve(HyperGraphExec hyperGraphExec,
                                  Set<String> prev,
                                  Map<Class<? extends HyperGraphExec>, HyperGraphExec> r) {
        List<HyperGraphExec> out = new ArrayList<>();
        prev.add(hyperGraphExec.getClass().getName());
        var notSorted = retrieveRecursive(hyperGraphExec, prev, r, out);
        var newSorted = new ArrayList<>(notSorted);
        newSorted.add(hyperGraphExec);
        return newSorted;
    }

    private @NotNull List<HyperGraphExec> retrieveRecursive(HyperGraphExec hyperGraphExec,
                                                            Set<String> prev,
                                                            Map<Class<? extends HyperGraphExec>, HyperGraphExec> r,
                                                            List<HyperGraphExec> out) {
        return (List<HyperGraphExec>) hyperGraphExec.dependsOnHyperNodes()
                .stream()
                .peek(s -> {
                    var sec = (Class<? extends HyperGraphExec>) s;
                    if (prev.contains(sec.getName())) {
                        throw new RuntimeException("Found cycle.");
                    }
                    prev.add(sec.getName());
                })
                .map(hg -> r.get((Class<? extends HyperGraphExec>) hg))
                .flatMap(s -> retrieve((HyperGraphExec) s, prev, r).stream())
                .collect(Collectors.toCollection(() -> out));
    }



    public Map<Class<? extends HyperGraphExec>, HyperGraphExec> retrieveHg() {
        return MapFunctions.CollectMap(
                metaGraph.sortedNodes()
                        .stream()
                        .map(m -> m.t().optional().stream())
                        .flatMap(h -> h instanceof HyperGraphExec hyper ? Stream.of(hyper) : Stream.empty())
                        .map(h -> Map.entry(h.getClass(), h))
        );
    }
    public Class<? extends TestGraphContext> getMatchingContext(HyperGraphExec hg) {
        return subGraphs.stream()
                .filter(sub ->  sub.clazz().equals(sub.dependsOn(hg)))
                .findAny()
                .map(SubGraph::clazz)
                .orElse(null);

    }

    public HyperGraphExec getMatchingContext(Class<? extends TestGraphContext> clazz) {
        return metaGraph.sortedNodes().stream()
                .flatMap(m -> m.t().optional().stream())
                .flatMap(h -> h instanceof HyperGraphExec hyper ? Stream.of(hyper) : Stream.empty())
                .filter(s -> subGraphs.stream()
                        .map(sub -> Objects.equals(sub.dependsOn(s), clazz) && sub.clazz().equals(clazz))
                        .filter(Boolean::booleanValue)
                        .findAny().orElse(false)
                )
                .findAny()
                .orElse(null);
    }

    public <T extends TestGraphNode>  List<T> retrieveNodes(Function<TestGraphNode, @Nullable T> clazz) {
        return retrieve(clazz, this.graphNodes);
    }

    public <T extends Graph>  Optional<T> retrieveGraph(Function<Graph, @Nullable T> clazz) {
        return retrieve(clazz, this.graphs).stream().findAny();
    }

    public <T extends TestGraphContext>  List<T> retrieveCtx(Function<TestGraphContext, @Nullable T> clazz) {
        return retrieve(clazz, this.graphCtxt);
    }

    private <T, U> List<T> retrieve(Function<U, @Nullable T> clazz,
                                    Map<Class<? extends U>, U> graphCtxt1) {
        return graphCtxt1.values().stream()
                .flatMap(g -> Stream.ofNullable(clazz.apply(g)))
                .toList();
    }

    private <T> void initializeMapNotProxy(List<T> graphNodes, Consumer<Map> n) {
        if (graphNodes.stream()
                .filter(ProxyUtil::isProxy)
                .peek(t -> log.error("Found proxy: {}", t))
                .findAny()
                .isPresent()) {
            throw new RuntimeException("Cannot accept proxies!");
        }

        var entryStream = graphNodes.stream()
                .map(t -> Map.entry((Class) t.getClass(), t))
                .map(m -> (Map.Entry) m)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        n.accept(entryStream);
    }


}
