package com.hayden.test_graph.graph;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.thread.ThreadScope;
import com.hayden.utilitymodule.MapFunctions;
import com.hayden.utilitymodule.proxies.ProxyUtil;
import jakarta.validation.constraints.Null;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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
    public void setGraphs(List<Graph> graphNodes) {
        initializeMapNotProxy(graphNodes, c -> this.graphs = c);
    }

    @Autowired
    @ThreadScope
    public void setNodes(List<TestGraphNode> graphNodes) {
        initializeMapNotProxy(graphNodes, c -> this.graphNodes = c);
    }

    @Autowired
    @ThreadScope
    public void setGraphContext(List<TestGraphContext> graphCtx) {
        initializeMapNotProxy(graphCtx, c -> this.graphCtxt = c);
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
                .filter(Predicate.not(ProxyUtil::isProxy))
                .peek(t -> log.error("Found proxy: {}", t))
                .findAny()
                .isPresent()) {
            throw new RuntimeException("Cannot accept proxies!");
        }

        n.accept(MapFunctions.Collect(
                graphNodes.stream().map(t -> Map.entry((Class<? extends T> )t.getClass(), t))
        ));
    }


}
