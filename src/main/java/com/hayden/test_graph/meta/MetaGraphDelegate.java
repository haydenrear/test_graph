package com.hayden.test_graph.meta;

import com.hayden.test_graph.ctx.HierarchicalContext;
import com.hayden.test_graph.ctx.HyperGraphContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.bubble.HyperGraphExec;
import com.hayden.test_graph.graph.Graph;
import com.hayden.test_graph.graph.SubGraph;
import com.hayden.test_graph.graph.node.TestGraphNode;
import com.hayden.test_graph.graph.service.TestGraphSort;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.exec.InitBubbleExec;
import com.hayden.test_graph.meta.graph.MetaGraph;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.MapFunctions;
import com.hayden.utilitymodule.proxies.ProxyUtil;
import com.hayden.utilitymodule.sort.GraphSort;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.PropertyValues;
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
@ResettableThread
public class MetaGraphDelegate {

    private Map<Class<? extends TestGraphNode>, TestGraphNode> graphNodes;

    private Map<Class<? extends TestGraphContext>, TestGraphContext> graphCtxt;

    private Map<Class<? extends Graph>, Graph> graphs;

    private Map<Class<? extends HyperGraphExec>, HyperGraphExec> hyperGraphExec;

    @Autowired
    @ResettableThread
    private List<? extends SubGraph<TestGraphContext<HyperGraphContext>, HyperGraphContext>> subGraphs;

    private MetaGraph metaGraph;

    @Autowired
    private TestGraphSort graphSort;

    @Autowired
    @ResettableThread
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
    @ResettableThread
    @Lazy
    public void setGraphs(List<Graph> graphNodes) {
        initializeMapNotProxy(graphNodes, c -> this.graphs = c);
    }

    @Autowired
    @ResettableThread
    @Lazy
    public void setNodes(List<TestGraphNode> graphNodes) {
        initializeMapNotProxy(graphNodes, c -> this.graphNodes = c);
    }

    @Autowired
    @ResettableThread
    @Lazy
    public void setGraphContext(List<TestGraphContext> graphCtx) {
        initializeMapNotProxy(graphCtx, c -> this.graphCtxt = c);
    }

    public List<HyperGraphExec> parseHyperGraph(Class<? extends TestGraphContext> clazz) {
        return retrieve(getMatchingContext(clazz));
    }

    public List<HyperGraphExec> retrieve(HyperGraphExec hyperGraphExec) {
        List<HyperGraphExec> toSort = hyperGraphExec.parseAllDeps(this.hyperGraphExec);
        return graphSort.sort(toSort);
    }

    /**
     * For each context type, such as InitCtx, DataDepCtx, there can exist some within context dependency graph, across defined contexts,
     * so then in this case the contexts that this context depends on as provided by clazz are retrieved and returned so that they can be
     * ran before. Assumed user provided Idempotency protection.
     * @param hg
     * @param clazz
     * @return
     */
    public Stream<Class<? extends TestGraphContext>> parseSubGraph(HyperGraphExec<TestGraphContext<HyperGraphContext>, HyperGraphContext> hg,
                                                                   Class<? extends TestGraphContext> clazz) {
        if (!hg.is(clazz))
            return Stream.empty();

        TestGraphContext testGraphContext = this.graphCtxt.get(clazz);

        List<TestGraphContext> matching =
                subGraphs.stream()
                        .filter(sub -> sub.clazz().equals(testGraphContext.bubbleClazz())
                                       && sub.clazz().equals(sub.dependsOn(hg)))
                        // retrieve all dependent bubble nodes
                        .flatMap(sub -> dependsOnRecursive(sub).stream())
                        .distinct()
                        .collect(Collectors.toCollection(ArrayList::new));


        matching.add(testGraphContext.bubble());

        List<TestGraphContext> sort = GraphSort.sort(matching);
        Stream<Class<? extends TestGraphContext>> sorted = sort
                .stream()
                .flatMap(tgc -> tgc instanceof HyperGraphContext<?> hgc
                                ? Stream.of(hgc)
                                : Stream.empty())
                .flatMap(tgc -> tgc
                        .bubblers().stream()
                        .flatMap(b -> Optional.ofNullable(this.graphCtxt.get(b)).stream())
                )
                .filter(HierarchicalContext::isLeafNode)
                .map(TestGraphContext::getClass);
        List<Class<? extends TestGraphContext>> list = sorted.toList();
        return list.stream();
    }

    private List<TestGraphContext> dependsOnRecursive(SubGraph<TestGraphContext<HyperGraphContext>, HyperGraphContext> sub) {
        return sub.dependsOnRecursive(this.graphCtxt);
    }

    public HyperGraphExec getMatchingContext(Class<? extends TestGraphContext> clazz) {
        var matching = metaGraph.sortedNodes().stream()
                .flatMap(m -> m.t().optional().stream())
                .flatMap(h -> h instanceof HyperGraphExec hyper
                              ? Stream.of(hyper)
                              : Stream.empty())
                .filter(s -> subGraphs.stream()
                        .map(sub -> sub.clazz().equals(clazz)
                                    && Objects.equals(sub.dependsOn(s), clazz))
                        .filter(Boolean::booleanValue)
                        .findAny().orElse(false)
                ).toList();

        if (matching.size() > 1) {
            log.error("Found multiple matching!");
        }

        return matching.stream().findAny().orElse(null);
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



}
