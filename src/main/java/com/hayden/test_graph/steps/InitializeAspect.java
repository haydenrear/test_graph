package com.hayden.test_graph.steps;

import com.hayden.test_graph.meta.exec.MetaProgExec;
import com.hayden.test_graph.thread.ResettableThread;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class InitializeAspect implements StepAspect {

    @ResettableThread
    @Autowired
    private MetaProgExec metaGraph;

    // TODO: this should probably go on Then steps, a list of them provided also as meta-annotations
    //      and then perform it only on Then steps to generalize setup across
    @Around("@annotation(initStep)")
    public Object around(ProceedingJoinPoint joinPoint, InitStep initStep) throws Throwable {
        var proceeded =  joinPoint.proceed();

        metaGraph.exec(initStep.value());

        return proceeded;
    }

}
