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
include(":fabric-1.19")
include(":fabric-1.21.11")
include(":fabric-26.2-dev")

include(":neoforge-1.21.1")
// include(":neoforge-1.20.1") — uncomment when building with Gradle 8.x
