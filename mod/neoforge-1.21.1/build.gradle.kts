plugins {
    id("net.neoforged.moddev") version "2.0.72"
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

base {
    archivesName.set("debugbridge-1.21.1-neoforge")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

sourceSets {
    main {
        java {
            srcDir(file("../core/src/main/java"))
        }
    }
}

dependencies {
    implementation("org.java-websocket:Java-WebSocket:1.6.0") {
        exclude(group = "org.slf4j")
    }
    implementation("org.apache.groovy:groovy:5.0.6")
    implementation("com.google.code.gson:gson:2.10.1")
}

configurations.implementation {
    resolutionStrategy {
        force("org.slf4j:slf4j-api:2.0.9")
    }
}

// Shadow: produce a fat JAR with dependencies for dev & production
tasks.shadowJar {
    archiveClassifier.set("")
    dependencies {
        include(dependency("org.java-websocket:Java-WebSocket:.*"))
        include(dependency("org.apache.groovy:groovy:.*"))
        // Gson excluded — NeoForge bundles it already
    }
    // Include the core module's web UI resources
    from(project(":core").tasks.named("processResources")) {
        include("webui/**")
    }
    // Exclude NeoForge / Minecraft content that leaks from the project output
    exclude("net/minecraft/**")
    exclude("net/neoforged/**")
    exclude("com/mojang/**")
    exclude("com/google/gson/**")  // already provided by NeoForge
    exclude("mcp/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/jarjar/**")
    exclude("META-INF/services/net.neoforged.**")
    // Avoid ZIP corruption from duplicate Groovy multi-release entries
    exclude("META-INF/versions/**")
}

neoForge {
    version = "21.1.87"
    runs {
        register("client") {
            client()
        }
    }
    mods {
        register("debugbridge") {
            sourceSet(sourceSets.main.get())
        }
    }
}

// Deploy Shadow JAR to run/mods/ so the fat JAR (with bundled deps) is used in dev
tasks.named("prepareClientRun") {
    dependsOn(tasks.named("shadowJar"))
    doLast {
        val runModsDir = layout.projectDirectory.dir("run/mods").asFile.also { it.mkdirs() }
        val jar = tasks.named("shadowJar").get().outputs.files.singleFile
        jar.copyTo(File(runModsDir, jar.name), overwrite = true)
        logger.lifecycle("[debugbridge] Deployed {} -> run/mods/", jar.name)
    }
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
