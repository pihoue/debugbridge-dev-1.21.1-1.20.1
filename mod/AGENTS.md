# DebugBridge Mod Notes

## Project Shape

- `core`: protocol, server, Lua bridge, mapping, snapshots, screenshots, textures, and provider interfaces. Version-agnostic.
- `forge-1.20.1`: Forge 1.20.1 adapters (ForgeGradle 6.x, Java 17).
- `neoforge-1.21.1`: NeoForge 1.21.1 adapters (NeoForge moddev, Java 21).

## Building

**命令行 Gradle** — 必须指定 `workdir = mod/`，否则 settings.gradle.kts 找不到。

```powershell
# Build Forge 1.20.1 JAR（用 Bash 工具，设 workdir = mod/）
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
gradlew.bat :forge-1.20.1:build --console=plain -q

# 只运行 spotless 格式化
gradlew.bat :forge-1.20.1:spotlessApply --console=plain -q

# 运行客户端
gradlew.bat :forge-1.20.1:runClient --console=plain

# 也可以用 idea_build_project 进行编译验证
# 用 idea_execute_run_configuration 运行构建/JAR/客户端
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
