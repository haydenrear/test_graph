package com.hayden.test_graph.step_def;

import com.google.common.collect.Sets;
import com.hayden.test_graph.meta.exec.MetaProgExec;
import com.hayden.test_graph.meta.graph.MetaGraph;
import com.hayden.test_graph.steps.InitStep;
import com.hayden.test_graph.steps.InitializeAspect;
import com.hayden.test_graph.test_init.*;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.MapFunctions;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestStepDef {

    @Autowired
    MockRegister mockRegister;
    @Autowired @ResettableThread
    MetaProgExec metaGraph;


    @When("I attempt to run")
    public void i_attempt_to_run() {
        // Write code here that turns the phrase above into concrete actions
    }

    @When("the data is initialized")
    @InitStep(TestInitChildCtx.class)
    public void the_data_is_initialized() {
        // Write code here that turns the phrase above into concrete actions
    }

    @Then("it runs")
    public void it_runs() {
        metaGraph.execAll();
        var classStream = Stream.of(
                        TestInitChildCtx.class, TestInitChildCtx.class, TestInitChildCtx.class, TestInitChildCtx.class,
                        InitReducer.class, InitReducer2.class,
                        InitReducer.class, InitReducer2.class,
                        TestInitBubbleNode.class,
                        TestInitChildDepNode.class, TestInitChildNode.class,
                        TestInitCtx.class, TestInitCtxDepNode.class,
                        TestInitCtxNode.class, TestInitParentCtx.class,
                        TestInitParentDepNode.class, TestInitParentNode.class
                )
                .toList();

        List<Class> mocks = mockRegister.getMocks();
        var not = classStream.stream().filter(Predicate.not(mocks::contains))
                        .toList();

        Assertions.assertEquals(0, not.size(), "Following did not exist: %s".formatted(not));

        Map<Class, Integer> dups = computeDups(mocks);
        var diff = Sets.difference(new HashSet<>(classStream), Set.of(TestInitChildCtx.class, InitReducer.class, InitReducer2.class));
        Assertions.assertNotEquals(0, diff);
        diff.forEach(d -> Assertions.assertFalse(dups.containsKey(d)));
    }


    private static @NotNull Map<Class, Integer> computeDups(List<Class> mocks) {
        Map<Class, Integer> dups = new HashMap<>();

        for (int i = 0; i < mocks.size(); ++ i) {
            Class aClass = mocks.get(i);
            if (mocks.subList(0, i).stream().anyMatch(aClass::equals))
                continue;

            for (int j = i; j < mocks.size(); ++ j) {
                if (i == j)
                    continue;

                if (mocks.get(i).equals(mocks.get(j))) {
                    dups.compute(mocks.get(i), (key, prev) -> prev == null ? 1 : prev + 1);
                }
            }
        }
        return dups;
    }


}
