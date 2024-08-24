package com.hayden.test_graph.meta.exec.prog_bubble;

import com.hayden.test_graph.graph.SubGraph;
import com.hayden.test_graph.test_init.TestInitChildCtx;
import com.hayden.test_graph.thread.ThreadScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class TestSubGraph {

    @SpringBootApplication
    @ComponentScan("com.hayden.test_graph")
    public static class TestMetaGraphApplication {
        public static void main(String[] args) {
            SpringApplication.run(TestMetaGraphApplication.class);
        }
    }

    @Autowired
    @ThreadScope
    List<SubGraph> subGraph;

    @Test
    public void testSubGraphAutoConfigure() {
        Assertions.assertFalse(subGraph.isEmpty());
        var tic = subGraph.stream().filter(s -> s.clazz().equals(TestInitChildCtx.class)).findAny();
        Assertions.assertTrue(tic.isPresent());
        var parsed = tic.get().parseContextTree();
        Assertions.assertEquals(3, parsed.size());
    }

}
