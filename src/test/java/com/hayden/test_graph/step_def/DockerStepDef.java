package com.hayden.test_graph.step_def;

import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class DockerStepDef implements ResettableStep {

    @Autowired
    @ResettableThread
    DockerInitCtx dockerInitCtx;

    @Given("docker-compose is started from {string}")
    public void docker_compose_started(String composePath) {
        dockerInitCtx.composePath().set(new File(composePath));
    }

    @And("build command {string} is added to be ran from {string}")
    public void buildCommand(String command, String composePath) {
        dockerInitCtx.getGradleTasks().add(new DockerInitCtx.GradleTask(command, composePath));
    }

}
