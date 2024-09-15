package com.hayden.test_graph.hook;

import com.hayden.test_graph.assertions.Assertions;
import io.cucumber.java.After;
import org.springframework.beans.factory.annotation.Autowired;

public class AssertionHook {

    @Autowired
    Assertions assertions;

//    @After
    public void after() {
        assertions.assertAll();
    }

}
