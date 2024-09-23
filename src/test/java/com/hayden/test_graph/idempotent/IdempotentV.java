package com.hayden.test_graph.idempotent;

import com.hayden.test_graph.action.DoRunAgain;
import com.hayden.test_graph.action.Idempotent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class IdempotentV {

    public void did() {}

    public void didAgain() {}

    public void didIf() {}


    @Idempotent
    public String doI() {
        did();
        return "hello";
    }

    @Idempotent
    public void doIAgain() {
        didAgain();
    }

    @Idempotent(runAgain = DoRunAgainIf.class)
    public void doIAgainVIf() {
        didIf();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoRunAgainIf implements DoRunAgain {
        int n = 0;

        @Override
        public boolean doRunAgain(Object[] args) {
            var rA = n == 0 || n == 2;
            n += 1;
            return rA;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoRunAgainIfTwo implements DoRunAgain {
        int n = 0;

        @Override
        public boolean doRunAgain(Object[] args) {
            var rA = n == 1 || n == 3;
            n += 1;
            return rA;
        }
    }

    @Idempotent(runAgain = DoRunAgainIfTwo.class)
    public String doIAgainIf(String inValue) {
        return inValue;
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
