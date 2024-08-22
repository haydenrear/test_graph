plugins {
    id("com.hayden.base-plugin")
    id("com.hayden.spring")
    id("com.hayden.no-main-class")
}

group = "com.hayden"
version = "1.0.0"

dependencies {
    implementation(project(":utilitymodule"))
}