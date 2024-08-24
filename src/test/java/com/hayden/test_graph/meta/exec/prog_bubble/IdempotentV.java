package com.hayden.test_graph.meta.exec.prog_bubble;

import com.hayden.test_graph.action.Idempotent;
import org.springframework.stereotype.Component;

@Component
public class IdempotentV {

    public void did() {}

    @Idempotent
    public String doI() {
        did();
        return "hello";
    }

}
