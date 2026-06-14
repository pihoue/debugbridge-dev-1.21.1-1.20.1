plugins {
    id("net.minecraftforge.gradle") version "6.0.+"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

base {
    archivesName.set("debugbridge-1.20.1-forge")
}

sourceSets {
    main {
        java {
            srcDir(file("../core/src/main/java"))
        }
    }
}

minecraft {
    mappings("official", "1.20.1")
    runs {
        create("client") {
            workingDirectory(project.file("runs/client"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            mods { create("debugbridge") { source(sourceSets.main.get()) } }
        }
    }
}

repositories {
    maven("https://maven.neoforged.net/releases/")
}

dependencies {
    minecraft("net.minecraftforge:forge:1.20.1-47.2.0")

    implementation("org.java-websocket:Java-WebSocket:1.6.0") { exclude("org.slf4j") }
    implementation("org.apache.groovy:groovy:5.0.6")
    implementation("com.google.code.gson:gson:2.14.0")
}

// Extract websocket + groovy into classes dir so Forge ModuleClassLoader finds them
val extractLibs = tasks.register("extractBridgeLibs") {
    dependsOn(configurations.runtimeClasspath)
    val out = sourceSets.main.get().output.classesDirs.first()
    inputs.files(configurations.runtimeClasspath)
    outputs.dir(out)
    doLast {
        configurations.runtimeClasspath.get()
            .filter { it.name.contains("Java-WebSocket") || it.name.contains("groovy") }
            .forEach { jar -> project.copy { from(project.zipTree(jar)); into(out) } }
    }
}
tasks.named("classes") { dependsOn(extractLibs) }

// Shadow: produce a clean mod JAR (the jar task inherits the bloated extract output)
tasks.shadowJar {
    archiveClassifier.set("")
    dependencies {
        include(dependency("org.java-websocket:Java-WebSocket"))
        include(dependency("org.apache.groovy:groovy"))
    }
    // Include web UI assets from the core module
    from(project(":core").tasks.named("processResources")) {
        include("webui/**")
    }
    exclude("net/minecraft/**")
    exclude("net/minecraftforge/**")
    exclude("com/mojang/**")
    exclude("com/google/gson/**")
    exclude("cpw/mods/**")
    exclude("mcp/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/jarjar/**")
    exclude("META-INF/versions/**")
}

tasks.withType<JavaCompile>().configureEach { options.release.set(17) }

tasks.processResources {
    // Include web UI assets from the core module
    from(project(":core").tasks.named("processResources")) {
        include("webui/**")
    }
    filesMatching("META-INF/mods.toml") {
        filter { line -> line.replace("\${version}", project.version.toString()) }
    }
}
