plugins {
    java
    id("com.diffplug.spotless") version "8.6.0" apply false
}

allprojects {
    group = "com.debugbridge"
    version = "2.0.0"

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
