package com.hayden.test_graph.meta.exec.prog_bubble;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
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
}
