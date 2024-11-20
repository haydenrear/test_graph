package com.hayden.test_graph.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Reporter {

    public void info(String toLog) {
        log.info(toLog);
    }

}
