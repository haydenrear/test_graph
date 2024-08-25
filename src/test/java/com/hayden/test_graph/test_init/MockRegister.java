package com.hayden.test_graph.test_init;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Getter
@Component
public class MockRegister {

    Set<Class> mocks = new HashSet<>();



}
