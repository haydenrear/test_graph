package com.hayden.test_graph.init.mountebank.ctx;

import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.mbtest.javabank.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ResettableThread
public interface MbInitCtx extends InitCtx {

    Client client();

    @Autowired
    void setClient(Client client);

}
