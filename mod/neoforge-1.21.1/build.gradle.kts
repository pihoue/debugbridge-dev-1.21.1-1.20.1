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
    implementation("org.luaj:luaj-jse:3.0.1")
    // Gson version pinned to match NeoForge 21.1.x bundled version
    implementation("com.google.code.gson:gson:2.10.1")

    jarJar("org.luaj:luaj-jse:3.0.1")
    jarJar("org.java-websocket:Java-WebSocket:1.6.0") {
        exclude(group = "org.slf4j")
    }
    // Gson excluded from jarJar — NeoForge bundles it already.
}

configurations.implementation {
    resolutionStrategy {
        force("org.slf4j:slf4j-api:2.0.9")
    }
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

// Deploy the built JAR (which bundles all deps) alongside the source-set mod
tasks.named("prepareClientRun") {
    dependsOn(tasks.named("jar"))
    doLast {
        val runMods = layout.projectDirectory.dir("run/mods").asFile.also { it.mkdirs() }
        val jar = tasks.named("jar").get().outputs.files.singleFile
        jar.copyTo(File(runMods, jar.name), overwrite = true)
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
