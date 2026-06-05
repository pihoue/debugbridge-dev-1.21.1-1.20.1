plugins {
    id("net.minecraftforge.gradle") version "6.0.+"
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
        create("server") {
            workingDirectory(project.file("runs/server"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            mods { create("debugbridge") { source(sourceSets.main.get()) } }
        }
        create("data") {
            workingDirectory(project.file("runs/data"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            args("--mod", "debugbridge", "--all", "--output", file("src/generated/resources/").absolutePath, "--existing", file("src/main/resources/").absolutePath)
            mods { create("debugbridge") { source(sourceSets.main.get()) } }
        }
    }
}

repositories {
    maven("https://maven.neoforged.net/releases/")
}

dependencies {
    minecraft("net.minecraftforge:forge:1.20.1-47.2.0")

    // annotation processor so mixin refmap is generated correctly
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    implementation("org.java-websocket:Java-WebSocket:1.6.0") { exclude("org.slf4j") }
    implementation("org.luaj:luaj-jse:3.0.1")
    implementation("com.google.code.gson:gson:2.14.0")
}

// Extract library classes into classes dir for dev TransformingClassLoader
val extractLibs = tasks.register("extractBridgeLibs") {
    dependsOn(configurations.runtimeClasspath)
    val out = sourceSets.main.get().output.classesDirs.singleFile
    inputs.files(configurations.runtimeClasspath)
    outputs.dir(out)
    doLast {
        configurations.runtimeClasspath.get()
            .filter { it.name.contains("Java-WebSocket") || it.name.contains("luaj") }
            .forEach { jar -> project.copy { from(project.zipTree(jar)); into(out) } }
    }
}
tasks.named("compileJava") { dependsOn(extractLibs) }

// Merge external deps into JAR for production
tasks.jar {
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .filter { it.name.contains("Java-WebSocket") || it.name.contains("luaj") || it.name.contains("gson") }
            .filter { !it.name.contains("forge-") && !it.name.contains("oshi") }
            .map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Merge service files from dependency JARs (e.g. SLF4J service loader)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .filter { it.name.contains("Java-WebSocket") || it.name.contains("luaj") }
            .map { zipTree(it) }
    }) {
        include("META-INF/services/*")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE // merge services
    }
}

tasks.withType<JavaCompile>().configureEach { options.release.set(17) }

tasks.processResources {
    filesMatching("META-INF/mods.toml") {
        filter { line -> line.replace("\${version}", project.version.toString()) }
    }
}
