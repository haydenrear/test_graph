package com.hayden.test_graph.test_init;

import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.init.exec.InitExec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.stereotype.Component;

@Component
public class InitReducer implements InitExec.InitReducer {

    @Autowired
    MockRegister mockRegister;

    @Override
    public InitBubble reduce(InitCtx first, InitBubble second) {
        mockRegister.mocks.add(this.getClass());
        return second;
    }
}
