pluginManagement {
    repositories {
        maven("https://maven.neoforged.net/releases/")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "debugbridge"

include(":neoforge-1.20.1")
