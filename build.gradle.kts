plugins {
    id("com.hayden.base-plugin")
    id("com.hayden.spring")
    id("com.hayden.observable-app")
    id("com.hayden.no-main-class")
    id("com.hayden.git")
    id("com.hayden.dgs-graphql")
    id("com.hayden.docker-compose")
    id("org.ndrwdn.mountebank") version "0.0.6"
}

group = "com.hayden"
version = "1.0.0"



dependencies {
    implementation(project(":utilitymodule"))
    implementation("org.junit.platform:junit-platform-suite")
    implementation("org.junit.jupiter:junit-jupiter-api")
    implementation("org.junit.platform:junit-platform-suite-api:1.11.0")
    implementation("io.cucumber:cucumber-spring:7.18.1")
    implementation("io.cucumber:cucumber-java:7.18.1")
    implementation("io.cucumber:cucumber-junit:7.18.1")
    implementation("io.cucumber:cucumber-junit-platform-engine:7.18.1")
    implementation("org.assertj:assertj-core:3.26.3")
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
    println("Getting mountebank!")
}

tasks.startMountebank {
    println("Starting mountebank!")
}

tasks.stopMountebank {
    println("Stopping mountebank!")
}

tasks.test {
    useJUnitPlatform()
    dependsOn("acquireMountebank", "startMountebank")
}

tasks.stopMountebank {
    dependsOn("test")
}
