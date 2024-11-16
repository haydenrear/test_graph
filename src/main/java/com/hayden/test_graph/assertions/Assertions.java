package com.hayden.test_graph.assertions;

import com.hayden.test_graph.thread.ResettableThread;
import lombok.experimental.Delegate;
import org.assertj.core.api.SoftAssertions;
import org.springframework.stereotype.Component;

@ResettableThread
@Component
public class Assertions {

    @Delegate
    private final SoftAssertions softAssertions = new SoftAssertions();

    public void assertStrongly(boolean v, String fail){
        assertStrongly(v, fail, "Assertion with failure message\n%s\nhas passed.".formatted(fail));
    }

    public void assertStrongly(boolean v, String fail, String success){
        assertThat(v)
                .withFailMessage(fail)
                .isTrue();
    }

}
