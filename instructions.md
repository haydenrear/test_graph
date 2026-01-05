# Test Graph

During integration testing we use an end to end framework called test graph. It builds a dynamic graph and executes the nodes in order, with edges and hypergraph edges.

The way that you work with test graph is by using test graph nodes and contexts. In fact, the contexts and the graph nodes both define dependencies defining the order of execution.

There are several node types, init, data dep, and assert. Each of the node types is actually a sub-graph, and each time you define a node type that also operates as a sub-graph. The bubble type defines the dependencies between nodes within a certain type. So for instance, if we were to implement an init subgraph, we would implement the InitCtx, and the InitBubbleCtx. So these would always run before any implementations of DataDepCtx or DataDepBubble. And then other people may implement InitCtx, and then you'd define whether or not you depend on their bubble inside of the dependsOn in your implementation of InitBubbleCtx. Then, within you're subgraph you'd have nodes, and you would extend InitNode with you're own interface, and then all of your nodes would then implement your extension of InitNode. Then you'd be able to define dependencies within you're own sub-graph between your own InitNode extension in the dependsOn of each of those nodes. You could technically also extend InitBubble and all that, but typically you wouldn't - any bubble is more of a marker interface for defining where your sub-graph is ordered amongst the other implementers of InitCtx.

So typically, there will be creating a new test graph, or extending an existing test graph. We use spring to make working with test graph easy. To create a new test graph we'll typically extend one of the interfaces for one of the existing test graph node types, such as those in the init or data dependency package, based on what phase of execution we want the test graph to exist in. For example init would be code we need to run to initialize the context, whereas data dependency would be code we need to run to set up the test harness or database to prepare for the test.

Then we'll create nodes from this. One of the functions in the interface for the contexts is a list of classes for contexts relating to dependency nodes that are ran first. So typically we'll define the interfaces and use spring to wire up the test graph. As an exmple, we can see in the existing test graph src/test/java in the commit_diff_contex package. You can see there some test setup nodes, the init and data dependency, and then the execution in the assert_nodes.

## Test Graph Execution and Architecture

There exist three test graph node types that we extend. The node types each have executors, and the ordering of execution within your sub-graph depends on the dependsOn function in the node you define, amongst other nodes of the same type in the bubble context dependsOn, and then the ordering between the three types is defined beforehand - and you can't change that. InitCtx -> DataDepCtx -> AssertCtx <- that's immutable. Maybe if you really need to you can define another phase, but that would be an addition to the library.

### Step Definitions, Graph Creation, and Assertions

The test graph uses cucumber and gherkin for execution. There are special annotations that we add to the step definitions to define the associated contexts. For example, in this code:

```java
@And("add episodic memory is called")
@RegisterInitStep(RepoOpInit.class)
public void add_commit_diff_context_episodic_memory() {
    commitDiffInit.getRepoInitializations().initItems()
            .add(new RepoInitItem.AddBlameNodes());
}

@Then("the episodic memory embeddings are validated to be added to the database")
@RegisterAssertStep(RepoOpAssertCtx.class)
public void initial_commit_diff_context_episodic_memory() {
    assertEpisodicMemorys();
    assertCommitDiffClusters();
    assertCommitDiffs();
    assertCommitDiffItems();
    assertions.assertSoftly(assertGitDiffEmbeddingItems(), "No git diff embeddings were embedded");
}
```

We register that the steps are either AssertCtx or InitCtx using the annotation. Test graph, behind the scenes, adds these to a queue, de-deduplicates, introspects, and then executes like this:

```java
@Override
public MetaCtx exec(Class<? extends TestGraphContext> ctx, MetaCtx metaCtx) {
//        assertDeps(ctx);
    for (var hgNode : lazyMetaGraphDelegate.parseHyperGraph(ctx)) {
        MetaCtx finalMetaCtx = metaCtx;
        Stream<Class<? extends TestGraphContext>> contextsRetrieved = lazyMetaGraphDelegate.parseSubGraph(hgNode, ctx);
        contextsRetrieved.map(c -> {
                    HyperGraphExec<TestGraphContext<HyperGraphContext>, HyperGraphContext> hgGraphExec
                            = edgeExec.preExecHgExecEdges(hgNode, finalMetaCtx);
                    String msg = "Executing %s".formatted(c.getName());
                    assertions.assertSoftly(Objects.nonNull(c), msg, msg);
                    var ctxCreated = hgGraphExec.exec((Class<? extends TestGraphContext<HyperGraphContext>>) c, finalMetaCtx);
                    MetaCtx m = edgeExec.postExecMetaCtxEdges(ctxCreated.bubbleMeta(finalMetaCtx), finalMetaCtx);
                    executed.add(c);
                    executed.add(ctxCreated.getClass());
                    return Map.entry(m, ctxCreated);
                })
                .forEach(mc -> {
                    if (mc.getKey() instanceof MetaProgCtx m) {
                        m.push(mc.getKey());
                        m.push(mc.getValue());
                    }
                });

        return finalMetaCtx;
    }

    return metaCtx;
}

@Override
public void register(Class<? extends TestGraphContext> ctx) {
    if (this.registered.contains(ctx)) {
        // ensure idempotency of calling them
        return;
    }

    this.registered.offer(ctx);
}

/**
 * Gets called on the first assert step - this removes the previous contexts registered one at a time,
 * executing them first, running each one only once, in the ordering of the last one inserted is kept,
 * assuming that it has dependencies on anything before it.
 */
@Override
public MetaCtx execAll() {
    Class<? extends TestGraphContext> ctx;

    List<Class<? extends TestGraphContext>> ordering = new ArrayList<>();

    while((ctx = registered.poll()) != null) {
        if (ordering.contains(ctx)) {
            // the ordering should be first because it's assumed that the first time it's
            //  requested it's needed in the following
            // don't do anything
        } else if (!executed.contains(ctx)) {
            ordering.add(ctx);
        }
    }

    for (var o : lazyMetaGraphDelegate.sort(ordering)) {
        log.info("Executing {}", o.getName());
        var next = this.exec(o);
        log.info("Executed {} - Result: {}", o.getName(), next.bubbleClazz().getName());
    }

    return this.metaProgCtx;
}

@Override
public int didExec() {
    var s = this.metaProgCtx.size();
    this.metaProgCtx.stream()
            .forEach(mc -> reportingValidationNodes.stream()
                    .filter(rn -> rn.matches(mc))
                    .forEach(rn -> rn.doValidateReport(mc)));
    return s;
}

@Override
public MetaCtx exec(Class<? extends TestGraphContext> ctx) {
    var n = exec(ctx, metaProgCtx);
    assertDeps(ctx);
    return n;
}
```

Everything is handled in the aspects from the above annotations, for example, the RegisterAssertStep aspect:

```java
@Aspect
@Component
public class AssertAspect implements StepAspect {

    @ResettableThread
    @Autowired
    private MetaProgExec metaGraph;

    @Around("@annotation(assertStep)")
    public Object around(ProceedingJoinPoint joinPoint, RegisterAssertStep assertStep) throws Throwable {

        Object ret;
        if (assertStep.doFnFirst()) {
            ret = joinPoint.proceed();
            doExec(assertStep);
        } else {
            doExec(assertStep);
            ret = joinPoint.proceed();
        }

        return ret;
    }

    private void doExec(RegisterAssertStep assertStep) {
        Arrays.stream(assertStep.value()).forEach(metaGraph::register);
    }

}
```

and the ExecInitStep aspect

```java
@Aspect
@Component
public class ExecInitAspect implements StepAspect {

    @ResettableThread
    @Autowired
    private MetaProgExec metaGraph;

    // TODO: this should probably go on Then steps, a list of them provided also as meta-annotations
    //      and then perform it only on Then steps to generalize setup across
    @Around("@annotation(initStep)")
    public Object around(ProceedingJoinPoint joinPoint, ExecInitStep initStep) throws Throwable {
        Object proceeded;
        if (initStep.after())
            proceeded =  joinPoint.proceed();
        else {
            Arrays.stream(initStep.value()).forEach(metaGraph::register);
            metaGraph.execAll();
            proceeded =  joinPoint.proceed();
        }

        return proceeded;
    }

}
```

So you can see that it starts executing when it sees an assert step, but just registers when it sees init steps. This is because the initialization code may need to be decoupled, etc. Also, we assume that all assertions are written in order in the gherkin, however initialization code can be de-duplicated or additional dependencies may be included transitively, as necessary, or based on properties or env configs.

## Test Graph Nodes

### Init

See an example in the com.hayden.test_graph.commit_diff_context.init. As a synopsis, there exists a DockerInitCtx and a Mountebanke based init context provided by the framework - so you can get an idea of how we'd use this to set up some of the architectural components, start the services, set up some mock services, etc. It's for setting up our kubernetes cluster, etc.

### Data Dependency

See an example in the com.hayden.test_graph.commit_diff_context.data_dep

Data dependency is typically for things like deleting from a database, or setting up a kafka topic, and things like that. It's not for things like setting up a kubernetes cluster, that would be an init!

### Assertion

See an example in the com.hayden.test_graph.commit_diff_context.assert_nodes.

The important part of assertion nodes is that they actually cause the execution of the rest of the test graph. If we only see initialization or data dep nodes, then why would we do any execution? We lazily execute only when we decide to assert something. This is all defined as per the above aspects.


## Graph Edges

We also need the ability to transfer data from context to context, and that's where edges come in. Hypergraph edges are added simply using spring's injection mechanism. As an example:

```java
@Component
@ResettableThread
public class IndexingDataDepToAssertEdge implements CommitDiffContextIndexingAssertNode {

    CommitDiffContextIndexingDataDepCtx dataDepBubble;

    @Autowired
    @ResettableThread
    public void setDataDepBubble(CommitDiffContextIndexingDataDepCtx dataDepBubble) {
        this.dataDepBubble = dataDepBubble;
    }

    @Override
    public CommitDiffContextIndexingAssertCtx exec(CommitDiffContextIndexingAssertCtx c, MetaCtx h) {
        this.set(c, dataDepBubble);
        return c;
    }

    void set(CommitDiffContextIndexingAssertCtx assertCtx, CommitDiffContextIndexingDataDepCtx dataDepBubble) {
        assertCtx.setDataDepContext(dataDepBubble);
    }
}
```

So you can see that all we need to do, then is add this node to the dependsOn for the test graph nodes in the sub-graph, to make sure that the context is transfered at the beginning. This allows us to keep the data and logic self-contained and have a breakpoint to use divide and conquer to find exactly which sub-graph the problem is in, and keep our domain objects small and able to mirror our source code.


## Assertions and Step Defs

The overarching idea behind the test graph is to map the gherkin to the step definitions, which set metadata in the contexts which are then used when the test graph is ran, or else run the assertion in the step definition. However, for the most part, in everything but the assert, which is the then clauses in Gherkin, we will actually only be setting configuration data in the contexts. So then these contexts define the dependencies in the test graph, and that auto-detects which nodes need to be ran and how those nodes are ran.

In the final steps, in the then clauses in the Gherkin, sometimes we can just assert in the step definition, but other times, we'll actually just be kicking off an assertion test graph. So in most cases, if you find yourself setting that something is complete or running an assertion in a given or a when clause in the step definition, that would be incorrect, because the test graph hasn't ran yet!

## Other pointers and sticking points

### Context Pointers

The init context needs to contain a reference typically to the bubble ctx, but we just inject it, like this:

```java
@Component
@ResettableThread
@RequiredArgsConstructor
public class CommitDiffContextIndexingDataDepCtx implements DataDepCtx {

    private CommitDiffContextIndexingDataDepBubble bubbleUnderlying;

    @Override
    public CommitDiffContextIndexingDataDepBubble bubble() {
        return this.bubbleUnderlying;
    }

    @Autowired
    @ResettableThread
    public void setBubble(CommitDiffContextIndexingDataDepBubble bubble) {
        this.bubbleUnderlying = bubble;
    }

    @Override
    public Class<CommitDiffContextIndexingDataDepBubble> bubbleClazz() {
        return CommitDiffContextIndexingDataDepBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffContextIndexingDataDepNode;
    }
    
}
```

It also needs to provide some type data. Similarly, the bubble context needs to tell the framework which contexts bubble it.
You see below a DataDepBubble is a bubble context that then defines a dependency on data dep bubble of a different data dep
sub-graph. So then the whole graph of CommitDiffDataDepBubble, and all nodes in that sub-graph will then run before the
nodes associated with this data dep context:

```java
@Component
@ResettableThread
@RequiredArgsConstructor
public class CommitDiffContextIndexingDataDepBubble implements DataDepBubble {

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CommitDiffContextIndexingDataDepBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(CommitDiffContextIndexingDataDepCtx.class);
    }
    
   
    @Override
    public List<Class<? extends TestGraphContext>> dependsOn() {
//       This is where you put dependencies in the same data dep node type (or init, or assert if it's that type of graph)
//          by setting the data dep context for the context of that sub-graph - if this class implemented InitBubble, 
//          then this would have to return a bubble context for a sub-graph that was also Init.
        return List.of(CommitDiffDataDepBubble.class);
    }
    
}
```

And you can also define dependencies within a particular data dep bubble sub-graph, and you just do it in the node itself.

Here you can see an edge node being added, and you can see where you could return the class references for the nodes within
the CommitDiffContextIndexingDataDepBubble subgraph.

So you can see here where a node defines it's dependencies, however this particular item is an edge and so should run
before every other node in the CommitDiffContextIndexingDataDepBubble subgraph.

```java
@Component
@ResettableThread
public class K3sToIndexingDataDepEdge implements CommitDiffContextIndexingDataDepNode {

    IndexingK3sInit indexingK3sInit;

    @Autowired
    @ResettableThread
    public void setK3sBubble(IndexingK3sInit indexingK3sInit) {
        this.indexingK3sInit = indexingK3sInit;
    }

    @Override
    public CommitDiffContextIndexingDataDepCtx exec(CommitDiffContextIndexingDataDepCtx c, MetaCtx h) {
        this.set(c, indexingK3sInit);
        return c;
    }
    
    @Override
    public List<Class<? extends CommitDiffContextIndexingDataDepNode>> dependsOn() {
        // this is where we'd define dependencies on the other CommitDiffContextIndexingDataDepNode
        // it can be a bit tricky because this dependsOn is a very 
        // generic **interface GraphNode extends GraphSort.GraphSortable**
        // which even hypergraph exec nodes implement...
        return new ArrayList<>();
    }
}

```

---

### Mountebank Nodes

Mountebank is a framework that allows us to mock various protocols, such as REST, WSS, etc. We integrate it in our test
graph as an init sub-graph. It's provided by the test graph, so it's a bit different than the other nodes, 
as you can see in java/com/hayden/test_graph/init/mountebank package - the mountebank has a base MbInitNode:

```java

public interface MbInitNode<T extends MbInitCtx> extends InitNode<T> {

//  this is the only one you have to implement, with you're own MbInitCtx!
    Stream<Imposter> createGetImposters(T ctx);

    @Override
    default T exec(T c, MetaCtx h) {
        createGetImposters(c)
                .forEach(imposterCreated -> createGetImposter(c, imposterCreated));
        return c;
    }

//    ... other code

}

```

So you can see, as an example, in src/test/java/com/hayden/test_graph/commit_diff_context/init/mountebank/commitdiff
and especially src/test/java/com/hayden/test_graph/commit_diff_context/init/mountebank/commitdiff/ModelServerResponseNode.java
where that node is provided

```java
@SneakyThrows
@Override
public Stream<Imposter> createGetImposters(CdMbInitCtx ctx) {
    var g = ctx.getServerResponses().responses().stream()
            .collect(Collectors.groupingBy(CdMbInitCtx.AiServerResponseDescriptor::responseType));

    return g.keySet().stream().sorted(RES)
            .map(r -> Map.entry(r, g.get(r)))
            .flatMap(entry -> getAiServerImposter(entry.getValue()));

}
```

And you can see in CdMbInitCtx, which we inject in the step definition file, it contains the state that we turn 
into mountebank imposters. So keep this in mind if you need to implement mountebank imposters. In some cases, you'll
just want to serialize a single json file into many imposters, saving only the file reference in your implementation
of MbInitCtx in the step definition, and then deserializing it and returning it in you're own mountebank node. However,
make sure you keep in mind that mountebank will fail to spin up on a port if that port is already being used! And
it may fail silently, even.

---