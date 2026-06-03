plugins {
    id("net.neoforged.gradle.userdev") version "7.0.183"
}

base {
    archivesName.set("debugbridge-1.20.1-neoforge")
}

dependencies {
    implementation(project(":core"))
    implementation("org.java-websocket:Java-WebSocket:1.6.0") {
        exclude(group = "org.slf4j")
    }
    implementation("net.neoforged:neoforge:20.4.237")


    jarJar(project(":core"))
    jarJar("org.luaj:luaj-jse:3.0.1")
    jarJar("org.java-websocket:Java-WebSocket:1.6.0")
    jarJar("com.google.code.gson:gson:2.14.0")
}

// Manual jarJar: merge core + external deps into JAR
tasks.jar {
    dependsOn(configurations.named("runtimeClasspath"))
    from({
        configurations.named("runtimeClasspath").get()
            .filter { it.name.endsWith(".jar") }
            .filter { it.name.contains("core-") || it.name.contains("luaj") || it.name.contains("Java-WebSocket") || it.name.contains("gson") }
            .map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

tasks.processResources {
    filesMatching("META-INF/neoforge.mods.toml") {
        filter { line -> line.replace("\${version}", project.version.toString()) }
    }
}


