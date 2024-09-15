plugins {
    id("com.hayden.base-plugin")
    id("com.hayden.spring")
    id("com.hayden.no-main-class")
    id("com.hayden.git")
    id("com.hayden.graphql")
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
}