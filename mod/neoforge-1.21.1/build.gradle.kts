plugins {
    id("net.neoforged.moddev") version "2.0.72"
}

base {
    archivesName.set("debugbridge-1.21.1-neoforge")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":core"))
    implementation("org.java-websocket:Java-WebSocket:1.6.0") {
        exclude(group = "org.slf4j")
    }

    jarJar(project(":core"))
    jarJar("org.luaj:luaj-jse:3.0.1")
    jarJar("org.java-websocket:Java-WebSocket:1.6.0")
    jarJar("com.google.code.gson:gson:2.14.0")
}

neoForge {
    version = "21.1.87"
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version)
    }
}
