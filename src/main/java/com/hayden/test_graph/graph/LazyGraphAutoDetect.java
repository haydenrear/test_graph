package com.hayden.test_graph.graph;

import com.hayden.test_graph.thread.ThreadScope;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Getter
@Component
public class LazyGraphAutoDetect {

    @Lazy
    @ThreadScope
    @Autowired
    @Delegate
    GraphAutoDetect autoDetect;

}
