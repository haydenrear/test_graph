plugins {
    id("com.hayden.base-plugin")
    id("com.hayden.observable-app")
    id("com.hayden.no-main-class")
    id("com.hayden.git")
    id("com.hayden.graphql")
    id("com.hayden.docker-compose")
    id("com.hayden.mb")
    id("com.hayden.cucumber")
    id("com.hayden.ai")
    id("com.hayden.jpa-persistence")
    id("com.hayden.paths")
    id("org.hibernate.orm") version "6.4.4.Final"
}


group = "com.hayden"
version = "1.0.0"

dependencies {
    implementation(project(":utilitymodule"))
    implementation(project(":commit-diff-model"))
    implementation(project(":commit-diff-context"))
//    implementation(project(":commit-diff-context-mcp"))
//    implementation(project(":multi-agent-ide"))
    implementation(project(":mcp-tool-gateway"))
    implementation(project(":jpa-persistence"))
    implementation(project(":proto"))
    implementation(project(":runner_code"))
    implementation(project(":libs-resolver"))
    implementation("software.amazon.awssdk:s3:2.38.2")
    implementation("io.kubernetes:client-java:24.0.0")
    implementation("org.seleniumhq.selenium:selenium-java:4.25.0")
}


// We may want to keep mountebank up to do things like save the proxied imposters, view the results, etc
val keepMountebankUp = project.property("keep-mountebank-up")?.toString()?.toBoolean()?.or(false) ?: false
val multiAgentIde = project.property("multi-agent-ide")?.toString()?.toBoolean()?.or(false) ?: false

tasks.compileJava {
//    TODO: add depends on docker task for each profile - so as to build the docker images for the workflow.
//    dependsOn("copyPromAgent")
//    dependsOn(project(":runner_code").tasks.getByName("runnerTask"))
//    dependsOn(project(":commit-diff-context-mcp").tasks.getByName("commitDiffContextMcpTask"))
//    dependsOn(project(":mcp-tool-gateway").tasks.getByName("mcpToolGatewayTask"))
    if (multiAgentIde)
        dependsOn(project(":multi_agent_ide").tasks.getByName("bootJar"))
//     java -javaagent:commit-diff-context/build/agent/prometheus-javaagent.jar=12345:commit-diff-context/prom-config.yaml -jar ?.jar
}

project.mountebank.allowInjection = true

tasks.acquireMountebank {
    project.logger.info("Getting mountebank!")
}

tasks.nodeSetup {
    project.logger.info("Doing node setup!")
    dependsOn("acquireMountebank")
}

tasks.startMountebank {
    project.logger.info("Starting mountebank!")
}



if (!keepMountebankUp) {
    tasks.stopMountebank {
        mustRunAfter(tasks.test, tasks.startMountebank)
        project.logger.info("Stopping mountebank!")
    }
}

tasks.test {
    dependsOn(tasks.acquireMountebank, tasks.startMountebank)
    useJUnitPlatform()
    if (!keepMountebankUp) {
        finalizedBy(tasks.stopMountebank)
    }
}

tasks.register("generateJUnitPlatformProperties") {
    val tags = System.getenv("cucumber.filter.tags") ?: System.getProperty("cucumber.filter.tags") ?: "@ag_ui_selenium_e2e"
    val file = project.projectDir.resolve("src/test/resources/junit-platform.properties")

    inputs.property("tags", tags)
    outputs.file(file)

    doLast {

        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }

        logger.info("JUnit platform properties with tag ${tags}.")

        val content = "cucumber.filter.tags=${tags}"
        file.writeText(content)
    }
}

tasks["compileTestJava"].dependsOn("processYmlFiles")
tasks["compileJava"].dependsOn("generateJUnitPlatformProperties", "processYmlFiles")
tasks["processTestResources"].dependsOn("generateJUnitPlatformProperties")
