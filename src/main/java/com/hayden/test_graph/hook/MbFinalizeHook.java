package com.hayden.test_graph.hook;

import org.mbtest.javabank.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MbFinalizeHook implements FinalizeHook {

    @Autowired
    Client client;

    @Override
    public Void call() throws Exception {
        client.deleteAllImposters();
        return null;
    }
}
