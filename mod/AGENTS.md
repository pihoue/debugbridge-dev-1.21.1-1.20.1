# DebugBridge Mod Notes

## Project Shape

- `core`: protocol, server, Lua bridge, mapping, snapshots, screenshots, textures, and provider interfaces. Version-agnostic.
- `forge-1.20.1`: Forge 1.20.1 adapters (ForgeGradle 6.x, Java 17).
- `neoforge-1.21.1`: NeoForge 1.21.1 adapters (NeoForge moddev, Java 21).

## Building

```powershell
# Build Forge 1.20.1 JAR（命令行，必须设 workdir = mod/）
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
.\gradlew.bat :forge-1.20.1:build --console=plain -q

# Build NeoForge 1.21.1 JAR
.\gradlew.bat :neoforge-1.21.1:shadowJar --console=plain -q

# 运行客户端
.\gradlew.bat :forge-1.20.1:runClient --console=plain
.\gradlew.bat :neoforge-1.21.1:runClient --console=plain

# 也可以用 idea_build_project / idea_execute_run_configuration
.\.editor/spotless.gradle :spotlessApply
```

> 注意：不同模块使用不同 Gradle 版本（forge = 8.10.2, neoforge = 9.5.1），切换模块前 `gradlew` 会自动下载对应版本。

## Testing

```powershell
.\gradlew.bat :core:test --console=plain
```

## Key Points

- Core compiled at Java 17 target (compatible with both branches).
- Forge 1.20.1 uses ForgeGradle 6.x (Gradle 8.10.2).
- NeoForge 1.21.1 uses NeoForge moddev plugin (Gradle 9.5.1).
- Both branches share the same `settings.gradle.kts` and `build.gradle.kts`.
