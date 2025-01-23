package com.hayden.test_graph.steps;

import com.hayden.test_graph.meta.exec.MetaProgExec;
import com.hayden.test_graph.thread.ResettableThread;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class AssertAspect implements StepAspect {

    @ResettableThread
    @Autowired
    private MetaProgExec metaGraph;

    @Around("@annotation(assertStep)")
    public Object around(ProceedingJoinPoint joinPoint, AssertStep assertStep) throws Throwable {

        Object ret;
        if (assertStep.doFnFirst()) {
            ret = joinPoint.proceed();
            doExec(assertStep);
        } else {
            doExec(assertStep);
            ret = joinPoint.proceed();
        }

        return ret;
    }

    private void doExec(AssertStep assertStep) {
        metaGraph.execAll();
        Arrays.stream(assertStep.value()).forEach(metaGraph::exec);
    }

}
