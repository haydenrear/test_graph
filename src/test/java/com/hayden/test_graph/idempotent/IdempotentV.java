package com.hayden.test_graph.idempotent;

import com.hayden.test_graph.action.Idempotent;
import org.springframework.stereotype.Component;

@Component
public class IdempotentV {

    public void did() {}
    public void didAgain() {}

    @Idempotent
    public String doI() {
        did();
        return "hello";
    }

    @Idempotent
    public void doIAgain() {
        didAgain();
    }

}
