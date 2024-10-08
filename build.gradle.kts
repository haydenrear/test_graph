plugins {
    id("com.hayden.base-plugin")
    id("com.hayden.spring")
    id("com.hayden.observable-app")
    id("com.hayden.no-main-class")
    id("com.hayden.git")
    id("com.hayden.graphql")
    id("com.hayden.docker-compose")
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
