package com.hayden.test_graph.commit_diff_context.init.commit_diff_init;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;


@Service
@Slf4j
@Transactional
public class RepoExecutor {


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void perform(Supplier<Void> doCall) {
        try {
            doCall.get();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
