package com.hayden.test_graph.meta;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.graph.Graph;
import com.hayden.test_graph.graph.SubGraph;
import com.hayden.test_graph.graph.node.TestGraphNode;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.meta.graph.MetaGraph;
import com.hayden.test_graph.thread.ThreadScope;
import com.hayden.utilitymodule.MapFunctions;
import com.hayden.utilitymodule.proxies.ProxyUtil;
import com.hayden.utilitymodule.sort.GraphSort;
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
public class MetaGraphDelegate {

    private Map<Class<? extends TestGraphNode>, TestGraphNode> graphNodes;

    private Map<Class<? extends TestGraphContext>, TestGraphContext> graphCtxt;

    private Map<Class<? extends Graph>, Graph> graphs;

    private Map<Class<? extends HyperGraphExec>, HyperGraphExec> hyperGraphExec;

    @Autowired
    @ThreadScope
    private List<? extends SubGraph> subGraphs;

    private MetaGraph metaGraph;

    @Autowired
    private TestGraphSort graphSort;

    @Autowired
    @ThreadScope
    public void setMetaGraph(MetaGraph metaGraph) {
        this.metaGraph = metaGraph;
        this.hyperGraphExec = MapFunctions.CollectMap(
                metaGraph.sortedNodes()
                        .stream()
                        .flatMap(m -> m.t().optional().stream())
                        .flatMap(h -> h instanceof HyperGraphExec hyper ? Stream.of(hyper) : Stream.empty())
                        .map(h -> Map.entry(h.getClass(), h))
        );
    }

    public <T extends TestGraphContext> Optional<T> getGraphContext(Class<T> clazz) {
        return Optional.ofNullable((T) this.graphCtxt.get(clazz));
    }

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
        return graphSort.sort(hyperGraphExec.parseAllDeps(this.hyperGraphExec));
    }

    /**
     * For each context type, such as InitCtx, DataDepCtx, there can exist some within context dependency graph, across defined contexts,
     * so then in this case the contexts that this context depends on as provided by clazz are retrieved and returned so that they can be
     * ran before. Assumed user provided Idempotency protection.
     * @param hg
     * @param clazz
     * @return
     */
    public Stream<Class<? extends TestGraphContext>> retrieveContextsToRun(HyperGraphExec hg, Class<? extends TestGraphContext> clazz) {
        Stream<TestGraphContext> matching = subGraphs.stream()
                .filter(sub -> sub.clazz().equals(sub.dependsOn(hg)))
                .flatMap(sub -> getGraphContext((Class<TestGraphContext>) sub.clazz())
                        .stream()
                        .flatMap(testGraphContext -> getGraphContext(clazz)
                                .map(tgc -> Map.entry(testGraphContext, tgc))
                                .stream()
                        )
                        .flatMap(contexts -> {
                            var testGraphContext = contexts.getKey();
                            var ctx = contexts.getValue();
                            return testGraphContext.getClass().equals(clazz) || ctx.dependsOnRecursive().contains(testGraphContext.bubbleClazz())
                                    ? Stream.of(testGraphContext)
                                    : Stream.empty();
                        }));

        return getSortedBubbles(matching)
                .peek(tgc -> {
                    System.out.printf("");
                })
                .map(TestGraphContext::getClass);
    }


    public HyperGraphExec getMatchingContext(Class<? extends TestGraphContext> clazz) {
        return metaGraph.sortedNodes().stream()
                .flatMap(m -> m.t().optional().stream())
                .flatMap(h -> h instanceof HyperGraphExec hyper ? Stream.of(hyper) : Stream.empty())
                .filter(s -> subGraphs.stream()
                        .map(sub -> sub.clazz().equals(clazz) && Objects.equals(sub.dependsOn(s), clazz))
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

    private <T> void initializeMapNotProxy(List<T> graphNodes, Consumer<Map<Class<? extends T>, T>> n) {
        if (graphNodes.stream()
                .filter(ProxyUtil::isProxy)
                .peek(t -> log.error("Found proxy: {}", t))
                .findAny()
                .isPresent()) {
            throw new RuntimeException("Cannot accept proxies!");
        }

        Map<Class<? extends T>, T> entryStream = graphNodes.stream()
                .map(t -> Map.entry((Class<? extends T>) t.getClass(), t))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k1));

        n.accept(entryStream);
    }

    private static @NotNull Stream<TestGraphContext> getSortedBubbles(Stream<TestGraphContext> matching) {
        List<TestGraphContext> toSortCtx = matching.toList();
        Stream<TestGraphContext> bubbleNodes = toSortCtx.stream().map(TestGraphContext::bubble);
        List<TestGraphContext> sortedBubbles = GraphSort.sort(bubbleNodes.toList());

        Stream<TestGraphContext> sortedDistinctBubbles = sortedBubbles.stream()
                .flatMap(tgc -> toSortCtx.stream().filter(t -> t.bubbleClazz().equals(tgc.getClass())))
                .distinct();
        return sortedDistinctBubbles;
    }

}
