# DebugBridge Mod Notes

## Project Shape

- `core`: protocol, server, Lua bridge, mapping, snapshots, screenshots, textures, and provider interfaces. Version-agnostic.
- `forge-1.20.1`: Forge 1.20.1 adapters (ForgeGradle 6.x, Java 17).
- `neoforge-1.21.1`: NeoForge 1.21.1 adapters (NeoForge moddev, Java 21).

## Building

**DO NOT use command-line Gradle** — 进程无法退出 + 路径错误。只使用 IntelliJ IDE 构建工具。

```powershell
# IntelliJ: Ctrl+F9 或 Run -> Build Project
# 使用 idea_build_project 工具进行编译验证
# 如需要运行客户端：在 Gradle 面板中找到 Tasks -> forge-1.20.1 -> runClient，双击执行
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
