plugins {
    id("net.minecraftforge.gradle") version "6.0.+"
    // Shadow: 包重定位，避免内嵌 luaj 时与其他模组发生类冲突
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

    implementation("org.java-websocket:Java-WebSocket:1.6.0") { exclude("org.slf4j") }
    implementation("org.luaj:luaj-jse:3.0.1")
    // Gson is already bundled by Forge — not included in the mod JAR
    implementation("com.google.code.gson:gson:2.14.0")
}

// Shadow: 将 luaj 包重定位到 com.debugbridge.luaj
// 这样即使其他 mod 也内嵌了 org.luaj，也不会产生类冲突
tasks.shadowJar {
    archiveClassifier.set("")

    dependencies {
        include(dependency("org.luaj:luaj-jse:.*"))
        include(dependency("org.java-websocket:Java-WebSocket:.*"))
    }

    relocate("org.luaj", "com.debugbridge.luaj")

    // 排除 Forge/Minecraft 运行环境类
    exclude("net/minecraft/**")
    exclude("net/minecraftforge/**")
    exclude("com/mojang/**")
    exclude("cpw/mods/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
    exclude("lua.class")
    exclude("luac.class")
    exclude("luajc*.class")
}

tasks.withType<JavaCompile>().configureEach { options.release.set(17) }

tasks.processResources {
    filesMatching("META-INF/mods.toml") {
        filter { line -> line.replace("\${version}", project.version.toString()) }
    }
}
