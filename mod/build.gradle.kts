plugins {
    id("com.diffplug.spotless") version "8.5.1" apply false
    id("dev.mixinmcp.decompile") version "0.9.0"
}

allprojects {
    group = "com.debugbridge"
    version = "1.1.2"

    repositories {
        mavenCentral()
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            target("src/**/*.java")
            palantirJavaFormat("2.38.0")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
