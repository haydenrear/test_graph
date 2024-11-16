package com.hayden.test_graph.services;

import org.mbtest.javabank.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("mb")
public class MountebankeServiceConnection implements ServiceConnection {

    @Autowired
    Client client;

    @Override
    public boolean isRunning() {
        return client.isMountebankRunning();
    }
}
