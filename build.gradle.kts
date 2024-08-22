plugins {
    id("com.hayden.base-plugin")
    id("com.hayden.spring")
    id("com.hayden.no-main-class")
}

group = "com.hayden"
version = "1.0.0"

dependencies {
    implementation(project(":utilitymodule"))
    implementation("io.cucumber:cucumber-spring:7.18.1")
    implementation("io.cucumber:cucumber-java:7.18.1")
}