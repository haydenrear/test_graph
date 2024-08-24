package com.hayden.test_graph.steps;

import com.hayden.test_graph.meta.exec.MetaProgExec;
import com.hayden.test_graph.thread.ThreadScope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class InitializeAspect {

    @ThreadScope
    @Autowired
    private MetaProgExec metaGraph;

    @Around("@annotation(initStep)")
    public Object around(ProceedingJoinPoint joinPoint, InitStep initStep) throws Throwable {
        var proceeded =  joinPoint.proceed();

        metaGraph.exec(initStep.value());

        return proceeded;
    }

}
