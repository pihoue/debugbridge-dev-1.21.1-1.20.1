plugins {
    id("net.fabricmc.fabric-loom") version "1.17.0-alpha.8"
}

base {
    archivesName.set("debugbridge-26.2-dev")
}

java {
    // 26.2 snapshots (verified against snapshot-6 and snapshot-7) declare a
    // Java 25 runtime in their version metadata; Modrinth launches them with
    // Zulu 25. JDK 25 is therefore sufficient to both compile and run; the
    // toolchain spec used to over-require JDK 26 (EA at the time of writing).
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    implementation(project(":core"))
    implementation("org.luaj:luaj-jse:3.0.1")
    implementation("org.java-websocket:Java-WebSocket:1.6.0")
    implementation("com.google.code.gson:gson:2.14.0")
    minecraft("com.mojang:minecraft:26.2-snapshot-7")
    implementation("net.fabricmc:fabric-loader:0.19.2")

    include(project(":core"))
    include("org.luaj:luaj-jse:3.0.1")
    include("org.java-websocket:Java-WebSocket:1.6.0")
    include("com.google.code.gson:gson:2.14.0")
}

tasks.withType<JavaCompile>().configureEach {
    // The current 26.2 snapshot metadata declares Java runtime 25.
    options.release.set(25)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
