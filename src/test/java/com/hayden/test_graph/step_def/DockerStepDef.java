package com.hayden.test_graph.step_def;

import com.hayden.test_graph.commit_diff_context.config.CommitDiffContextConfigProps;
import com.hayden.test_graph.config.EnvConfigProps;
import com.hayden.test_graph.init.docker.ctx.DockerInitCtx;
import com.hayden.test_graph.steps.ResettableStep;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.io.FileUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Paths;

public class DockerStepDef implements ResettableStep {

    @Autowired
    @ResettableThread
    DockerInitCtx dockerInitCtx;
    @Autowired
    CommitDiffContextConfigProps props;
    @Autowired
    EnvConfigProps env;

    @Given("docker-compose is started from {string}")
    public void docker_compose_started(String composePath) {
        dockerInitCtx.composePath()
                .swap(FileUtils.replaceHomeDir(Paths.get(props.getHomeDir()), composePath));
    }

    @And("docker container from repo {string} with branch {string} is built with image name {string} from subdirectory {string} and dockerfile {string}")
    public void buildCommand(String repo, String branch, String imageName, String subdirectory, String dockerfile) {
        if (!repo.startsWith("http") && !repo.startsWith("git") && !repo.startsWith("ssh")) {
            repo = FileUtils.replaceHomeDir(env.getHomeDir(), repo).toPath().toAbsolutePath().toString();
        }
        dockerInitCtx.getDockerBuildCommands().add(new DockerInitCtx.DockerTask.BuildCloneDockerTask(repo, branch,  subdirectory, imageName, dockerfile));
    }

    @And("the docker container {string} exists")
    public void theDockerContainerExists(String containerToAssert) {
        dockerInitCtx.getContainers().add(new DockerInitCtx.AssertContainer(containerToAssert));
    }
}
