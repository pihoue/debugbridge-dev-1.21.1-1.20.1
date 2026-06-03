import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.JavaVersion

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

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(17)
    }

    configure<SpotlessExtension> {
        java {
            target("src/**/*.java")
            palantirJavaFormat("2.38.0")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
