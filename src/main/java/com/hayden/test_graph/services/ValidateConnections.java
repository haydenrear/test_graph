package com.hayden.test_graph.services;

import com.hayden.test_graph.assertions.Assertions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class ValidateConnections {

    @Autowired(required = false)
    List<ServiceConnection> serviceConnections;

    @PostConstruct
    public void validateAllServices() {
        assert Optional.ofNullable(serviceConnections)
                .stream().flatMap(Collection::stream)
                .allMatch(ServiceConnection::isRunning);
    }

}
