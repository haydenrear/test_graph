plugins {
    id("com.hayden.base-plugin")
    id("com.hayden.observable-app")
    id("com.hayden.no-main-class")
    id("com.hayden.git")
    id("com.hayden.dgs-graphql")
    id("com.hayden.docker-compose")
    id("com.hayden.mb")
    id("com.hayden.cucumber")
    id("com.hayden.ai")
    id("com.hayden.jpa-persistence")
    id("org.hibernate.orm") version "6.4.4.Final"
}


group = "com.hayden"
version = "1.0.0"

dependencies {
    implementation(project(":utilitymodule"))
    implementation(project(":commit-diff-model"))
    implementation(project(":jpa-persistence"))
    implementation(project(":proto"))
}

tasks.compileJava {
    dependsOn("copyPromAgent")
//     java -javaagent:commit-diff-context/build/agent/prometheus-javaagent.jar=12345:commit-diff-context/prom-config.yaml -jar ?.jar
}

tasks.generateJava {
    typeMapping = mutableMapOf(
        Pair("ByteArray", "com.hayden.commitdiffmodel.scalar.ByteArray")
    )
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

tasks.stopMountebank {
    mustRunAfter(tasks.test, tasks.startMountebank)
    project.logger.info("Stopping mountebank!")
}

tasks.test {
    dependsOn(tasks.acquireMountebank, tasks.startMountebank)
    useJUnitPlatform()
    finalizedBy(tasks.stopMountebank)
}

tasks.register("generateJUnitPlatformProperties") {
    val tags = System.getenv("cucumber.filter.tags") ?: System.getProperty("cucumber.filter.tags") ?: "@all"
    val file = project.projectDir.resolve("src/test/resources/junit-platform.properties")

    inputs.property("tags", tags)
    outputs.file(file)

    doLast {

        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }

        logger.info("JUnit platform properties with tag ${tags}.")

        val content = """
cucumber.filter.tags=${tags}
"""
        file.writeText(content)
    }
}

tasks["compileJava"].dependsOn("generateJUnitPlatformProperties")
tasks["processTestResources"].dependsOn("generateJUnitPlatformProperties")
