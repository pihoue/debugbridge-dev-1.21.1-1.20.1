import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.diffplug.spotless") version "8.5.1" apply false
}

allprojects {
    group = "com.debugbridge"
    version = "1.1.0"

    repositories {
        mavenCentral()
        maven("https://maven.neoforged.net/releases/")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.diffplug.spotless")

    configure<SpotlessExtension> {
        java {
            target("src/**/*.java")
            palantirJavaFormat("2.90.0")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
