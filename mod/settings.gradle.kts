pluginManagement {
    repositories {
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "debugbridge"

include(":core")

val gradleBase = org.gradle.util.GradleVersion.current().baseVersion
if (gradleBase < org.gradle.util.GradleVersion.version("9.0")) {
    include(":forge-1.20.1")
}
include(":neoforge-1.21.1")
