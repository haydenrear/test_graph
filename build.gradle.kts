plugins {
    id("com.hayden.base-plugin")
    id("com.hayden.observable-app")
    id("com.hayden.no-main-class")
    id("com.hayden.git")
    id("com.hayden.dgs-graphql")
    id("com.hayden.docker-compose")
    id("com.hayden.mb")
    id("com.hayden.cucumber")
}


group = "com.hayden"
version = "1.0.0"

dependencies {
    implementation(project(":utilitymodule"))
}

tasks.compileJava {
    dependsOn("copyPromAgent")
//     java -javaagent:commit-diff-context/build/agent/prometheus-javaagent.jar=12345:commit-diff-context/prom-config.yaml -jar ?.jar
}

tasks.register<Copy>("copyGraphQlSchema") {
    from(project.rootDir.toPath().resolve("commit-diff-context/src/main/resources/schema"))
    into(projectDir.resolve("src/main/resources/schema"))
}

tasks.generateJava {
    schemaPaths.add("${projectDir}/src/main/resources/schema")
    packageName = "com.hayden.test_graph.codegen"
    generateClient = true
    typeMapping = mutableMapOf(
        Pair("ByteArray", "com.hayden.test_graph.config.ByteArray")
    )
}

tasks.named("generateJava").configure { dependsOn("copyGraphQlSchema") }
tasks.named("processResources").configure { dependsOn("copyGraphQlSchema") }

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

tasks.stopMountebank {
    mustRunAfter(tasks.test, tasks.startMountebank)
    project.logger.info("Stopping mountebank!")
}

tasks.test {
    dependsOn(tasks.acquireMountebank, tasks.startMountebank)
    useJUnitPlatform()
    finalizedBy(tasks.stopMountebank)
}


