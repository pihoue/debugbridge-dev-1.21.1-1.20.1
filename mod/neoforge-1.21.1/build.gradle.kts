plugins {
    id("net.neoforged.moddev") version "2.0.72"
    // Shadow: 包重定位，避免内嵌 luaj 时与其他模组发生 JPMS/classpath 冲突
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
    // luaj 作为编译期依赖（core 源码使用），不由 jarJar 直接嵌入
    implementation("org.luaj:luaj-jse:3.0.1")
    // Gson version pinned to match NeoForge 21.1.x bundled version
    implementation("com.google.code.gson:gson:2.10.1")

    // luaj 不由 jarJar 直接嵌入，而是由 Shadow 重定位后打包
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

// Shadow: 将 luaj 包重定位到 com.debugbridge.luaj
// 这样即使其他模组也内嵌 org.luaj，也不会产生类冲突
tasks.shadowJar {
    archiveClassifier.set("")

    // 仅打包 luaj 和 websocket，不打包 Minecraft/NeoForge 等运行环境类
    dependencies {
        include(dependency("org.luaj:luaj-jse:.*"))
        include(dependency("org.java-websocket:Java-WebSocket:.*"))
    }

    relocate("org.luaj", "com.debugbridge.luaj")

    // 排除 Minecraft/NeoForge 游戏资源、运行环境依赖和无关工具类
    exclude("assets/**")
    exclude("data/**")
    exclude("net/minecraft/**")
    exclude("net/neoforged/**")
    exclude("com/mojang/**")
    exclude("mcp/**")
    exclude("lua.class")           // luaj CLI 启动器（非库类）- 精确匹配避免误伤
    exclude("luac.class")
    exclude("luajc*.class")
    exclude("META-INF/jarjar/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
    exclude("META-INF/services/net.neoforged.**")
}

// Deploy the Shadow JAR (relocated luaj + bundled ws) alongside the source-set mod
tasks.named("prepareClientRun") {
    dependsOn(tasks.named("shadowJar"))
    doLast {
        val runMods = layout.projectDirectory.dir("run/mods").asFile.also { it.mkdirs() }
        val jar = tasks.named("shadowJar").get().outputs.files.singleFile
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
