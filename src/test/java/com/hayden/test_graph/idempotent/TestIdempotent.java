package com.hayden.test_graph.idempotent;


import com.hayden.test_graph.meta.exec.prog_bubble.TestMetaGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.times;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class TestIdempotent {

    @SpringBootApplication
    @ComponentScan("com.hayden.test_graph")
    public static class TestMetaGraphApplication {
        public static void main(String[] args) {
            SpringApplication.run(TestMetaGraph.TestMetaGraphApplication.class);
        }
    }

    @SpyBean
    IdempotentV v;

    @Test
    public void test() {
        v.doI();
        v.doI();
        v.doI();

        Mockito.verify(v, times(1)).did();
    }

    @Test
    public void testNull() {
        v.doIAgain();
        v.doIAgain();
        v.doIAgain();

        Mockito.verify(v, times(1)).didAgain();
    }

    @Test
    public void testW() {
        var f = v.doIWArg("whatever");
        var q = v.doIWArg("whatever");
        Assertions.assertAll(
                () -> Assertions.assertEquals("goodbye", f),
                () -> Assertions.assertEquals("whatever", q)
        );
    }

    @Test
    public void testIAgainIf() {
        var f = v.doIAgainIf("okay");
        var q = v.doIAgainIf("whatever");

        Assertions.assertAll(
                () -> Assertions.assertEquals("okay", f),
                () -> Assertions.assertEquals("okay", q)
        );

        v.doIAgainVIf();
        Mockito.verify(v, times(1)).didIf();
        v.doIAgainVIf();
        v.doIAgainVIf();
        Mockito.verify(v, times(2)).didIf();
        v.doIAgainVIf();
        Mockito.verify(v, times(3)).didIf();
    }
}
