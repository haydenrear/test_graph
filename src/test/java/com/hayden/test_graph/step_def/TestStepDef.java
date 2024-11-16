package com.hayden.test_graph.step_def;

import com.hayden.test_graph.steps.InitStep;
import com.hayden.test_graph.steps.InitializeAspect;
import com.hayden.test_graph.test_init.*;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class TestStepDef {

    @Autowired
    MockRegister mockRegister;


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

        var classStream = Stream.of(
//                      gets called a couple more times...
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
        var not = classStream.stream().filter(Predicate.not(mockRegister.getMocks()::contains))
                        .toList();

        Assertions.assertEquals(mockRegister.getMocks().size(), classStream.size() + 1);
        Assertions.assertEquals(0, not.size(), "Following did not exist: %s".formatted(not));
    }


}
