package com.hayden.test_graph.meta.exec.prog_bubble;

import com.hayden.test_graph.meta.exec.MetaProgExec;
import com.hayden.test_graph.meta.graph.MetaGraph;
import com.hayden.test_graph.test_init.TestInitChildCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class TestMetaGraph {

    @SpringBootApplication
    @ComponentScan("com.hayden.test_graph")
    public static class TestMetaGraphApplication {
        public static void main(String[] args) {
            SpringApplication.run(TestMetaGraphApplication.class);
        }
    }

    @Autowired
    @ResettableThread
    MetaGraph metaGraph;
    @Autowired
    @ResettableThread
    MetaProgExec exec;

    @Test
    public void test() {
        var e = exec.exec(TestInitChildCtx.class);
        Assertions.assertNotNull(e);
    }

}
