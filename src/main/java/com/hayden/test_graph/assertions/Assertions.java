package com.hayden.test_graph.assertions;

import com.hayden.test_graph.report.Reporter;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@ResettableThread
@Component
@Slf4j
public class Assertions {

    @Delegate
    private final SoftAssertions softAssertions = new SoftAssertions();

    @Autowired
    private Reporter reporter;

    public void assertStrongly(boolean v, String fail){
        assertStrongly(v, fail, "Assertion with failure message\n%s\nhas passed.".formatted(fail));
    }

    public void assertStrongly(boolean v, String fail, String success){
        assertThat(v)
                .withFailMessage(fail)
                .isTrue();
    }

    public void assertSoftly(boolean v, String fail){
        assertSoftly(v, fail, "Assertion with failure message\n%s\nhas passed.".formatted(fail));
    }

    public void assertStronglyPattern(boolean v, String fail, Object... args){
        if (!v) {
            assertStrongly(v, fail.formatted(args));
        }
    }

    public void assertSoftlyPattern(boolean v, String fail, Object... args){
        if (!v) {
            assertSoftly(v, fail.formatted(args));
        }
    }

    public void reportAssert(String message) {
        assertSoftly(true, "", message);
    }

    public void reportAssert(String message, Object... args) {
        log.info("{}", message.formatted(args));
        assertSoftly(true, "", message.formatted(args));
    }

    public void assertSoftly(boolean v, String fail, String success){
        softAssertions.assertThat(v)
                .withFailMessage(fail)
                .isTrue();
        if (v)
            reporter.info(success);

    }

    public void assertSoftly(boolean v, String fail, Supplier<String> success){
        softAssertions.assertThat(v)
                .withFailMessage(fail)
                .isTrue();
        if (v)
            reporter.info(success.get());

    }

}
