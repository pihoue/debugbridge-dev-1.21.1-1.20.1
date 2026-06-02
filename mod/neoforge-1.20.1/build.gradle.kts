plugins {
    id("net.neoforged.gradle.userdev") version "7.0.183"
}

base {
    archivesName.set("debugbridge-1.20.1-neoforge")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(project(":core"))
    implementation("org.java-websocket:Java-WebSocket:1.6.0") {
        exclude(group = "org.slf4j")
    }

    // NeoForge 1.20.1-47.1.0+ uses Mojang mappings by default
    implementation("net.neoforged:neoforge:20.4.237")

    // Bundle core + runtime dependencies via Jar-in-Jar
    jarJar(project(":core"))
    jarJar("org.luaj:luaj-jse:3.0.1")
    jarJar("org.java-websocket:Java-WebSocket:1.6.0")
    jarJar("com.google.code.gson:gson:2.14.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version)
    }
}
