/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.4.2/userguide/building_java_projects.html
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:30.1.1-jre")

    // 对 `core` 模块的依赖
    implementation(project(":core"))
}

application {
    // Define the main class for the application.
    mainClass.set("work.chiro.game.application.Main")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

tasks.withType<Test> {
    systemProperty("file.encoding", "UTF-8")
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.jar {
    enabled = true
    manifest {
        attributes(mapOf("Main-Class" to "work.chiro.game.application.Main"))
    }
    val sourcesMain = sourceSets.main.get()
    from(sourcesMain.output)
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
