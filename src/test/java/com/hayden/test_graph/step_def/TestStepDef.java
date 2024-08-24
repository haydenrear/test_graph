package com.hayden.test_graph.step_def;

import com.hayden.test_graph.steps.InitStep;
import com.hayden.test_graph.steps.InitializeAspect;
import com.hayden.test_graph.test_init.TestInitChildCtx;
import com.hayden.test_graph.test_init.TestInitCtx;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class TestStepDef {


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
        // Write code here that turns the phrase above into concrete actions
    }


}
