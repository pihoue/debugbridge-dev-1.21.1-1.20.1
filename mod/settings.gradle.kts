pluginManagement {
    repositories {
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "debugbridge"

include(":core")
// NOTE: userdev 7.x requires Gradle 8.x — wrapper is pre-configured for 8.10.2
include(":neoforge-1.20.1")
