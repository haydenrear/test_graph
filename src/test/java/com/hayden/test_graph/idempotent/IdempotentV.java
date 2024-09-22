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

    public String didWArg(String arg) {
        return arg;
    }

    public String didAgainWArg(String argAgain) {
        return argAgain;
    }

    @Idempotent(returnArg = 0)
    public String doIWArg(String in) {
        return didWArg("goodbye");
    }

    @Idempotent(returnArg = 0)
    public String doIAgainWArg(String in) {
        return didAgainWArg("goodbye");
    }
}
