# DebugBridge Mod Notes

## Project Shape

- `core`: protocol, server, Lua bridge, mapping, snapshots, screenshots, textures, and provider interfaces. Version-agnostic.
- `forge-1.20.1`: Forge 1.20.1 adapters (ForgeGradle 6.x, Java 17).
- `neoforge-1.21.1`: NeoForge 1.21.1 adapters (NeoForge moddev, Java 21).

## Building

```powershell
# Forge 1.20.1
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
.\gradlew.bat :forge-1.20.1:runClient --console=plain

# NeoForge 1.21.1
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
.\gradlew.bat :neoforge-1.21.1:runClient --console=plain
```

## Testing

```powershell
.\gradlew.bat :core:test --console=plain
```

## Key Points

- Core compiled at Java 17 target (compatible with both branches).
- Forge 1.20.1 uses ForgeGradle 6.x (Gradle 8.10.2).
- NeoForge 1.21.1 uses NeoForge moddev plugin (Gradle 9.5.1).
- Both branches share the same `settings.gradle.kts` and `build.gradle.kts`.
