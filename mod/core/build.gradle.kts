plugins {
    java
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

dependencies {
    // Groovy - JVM scripting runtime for the `execute` endpoint (mapping-aware
    // Java/Minecraft interop). Replaces the former LuaJ runtime.
    implementation("org.apache.groovy:groovy:5.0.6")

    // Java-WebSocket - lightweight WebSocket server
    implementation("org.java-websocket:Java-WebSocket:1.6.0")

    // Gson - JSON serialization
    implementation("com.google.code.gson:gson:2.14.0")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// ---- Bundled web UI (best-effort) ----
// The Vue app's production build ships inside this jar under /webui.  Build
// is skipped when the web-ui checkout is incomplete (CI clones it separately).
val webUiDir = rootProject.projectDir.resolve("../web-ui").normalize()
val webUiConfig = webUiDir.resolve("vite.config.ts")
if (webUiConfig.exists()) {
    val npm = if (System.getProperty("os.name").lowercase().contains("win")) "npm.cmd" else "npm"

    val webUiInstall = tasks.register<Exec>("webUiInstall") {
        description = "npm ci for the web UI (skipped when node_modules matches the lockfile)"
        workingDir = webUiDir
        commandLine(npm, "ci")
        inputs.file(webUiDir.resolve("package-lock.json"))
        outputs.file(webUiDir.resolve("node_modules/.package-lock.json"))
    }

    val webUiBuild = tasks.register<Exec>("webUiBuild") {
        description = "Vite production build of the web UI"
        dependsOn(webUiInstall)
        workingDir = webUiDir
        commandLine(npm, "run", "build")
        environment("VITE_BASE", "./")
        inputs.dir(webUiDir.resolve("src"))
        inputs.files(
                webUiDir.resolve("index.html"),
                webUiDir.resolve("package.json"),
                webUiDir.resolve("vite.config.ts"),
                webUiDir.resolve("tailwind.config.js"),
                webUiDir.resolve("postcss.config.js"))
        outputs.dir(webUiDir.resolve("dist"))
    }

    tasks.processResources {
        from(webUiBuild) { into("webui") }
    }
} else {
    logger.warn("[debugbridge] web-ui config not found at $webUiConfig — skipping UI build")
}
