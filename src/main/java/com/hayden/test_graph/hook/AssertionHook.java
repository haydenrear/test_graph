package com.hayden.test_graph.hook;

import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.meta.exec.MetaProgExec;
import com.hayden.test_graph.report.Reporter;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.test_graph.thread.ResettableThreadLike;
import com.hayden.test_graph.thread.ResettableThreadScope;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.presentation.StandardRepresentation;
import org.mbtest.javabank.Client;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AssertionHook implements ResettableThreadLike {

    @Autowired
    @ResettableThread
    Assertions assertions;
    @Autowired
    @ResettableThread
    MetaProgExec metaProgExec;

    @Autowired
    List<FinalizeHook> finalizers;

    @After
    public void after() {
        metaProgExec.execAll();
        int numGraphExecutions = metaProgExec.didExec();
        assertions.assertSoftly(numGraphExecutions > 0, "Meta Program did not execute any computation graphs: %s.".formatted(numGraphExecutions),
                "Meta Program did execute this many computation graphs: %s.".formatted(numGraphExecutions));
        finalizers.forEach(f -> {
            try {
                f.call();
            } catch (Exception e) {
                assertions.assertSoftly(false, "Failed to call finalizer: %s.".formatted(f), e.getMessage());
            }
        });

        assertions.assertAll();
    }


}
