package com.hayden.test_graph.assertions;

import com.hayden.test_graph.thread.ThreadScope;
import lombok.experimental.Delegate;
import org.assertj.core.api.SoftAssertions;
import org.springframework.stereotype.Component;

@ThreadScope
@Component
public class Assertions {

    @Delegate
    private final SoftAssertions softAssertions = new SoftAssertions();

}
