# DebugBridge Mod Architecture

支持两个 Minecraft 版本，共享同一套 `core` 模块源码。

## Module Boundaries

- `core`: no DebugBridge module dependencies.
- `forge-1.20.1`: depends on `core` and on Minecraft/Forge APIs.
- `neoforge-1.21.1`: depends on `core` and on Minecraft/NeoForge APIs.

## Runtime Model

- `BridgeServer` owns the localhost WebSocket protocol.
- `ScriptRuntime` executes Groovy scripts through the Java bridge and dispatches Minecraft state access onto the game thread.
- Provider interfaces in `core` expose native fast paths for snapshots, screenshots, entities, blocks, screen inspection, chat history, and item textures.
- `forge-1.20.1` and `neoforge-1.21.1` modules register version-specific providers.

## LuaJ 依赖管理（避免 JPMS/Classpath 冲突）

`org.luaj:luaj-jse:3.0.1` 是 DebugBridge 的嵌入式 Lua 5.2 运行时。为防止与其他同样内嵌 luaj 的模组发生类冲突，采用以下策略：

### 原则

1. **不将 `org.luaj` 包内嵌在源码或资源中** — luaj 仅作为 Maven 依赖引入，不使用 jarJar 直接嵌入。
2. **桥接模块隔离** — 所有 `org.luaj` 的 import 集中在 `core` 模块的 `LuaRuntime`、`JavaBridge`、`MethodCallWrapper` 中，forge/neoforge 模块不直接引用 luaj 类型。
3. **包重定位（Shadow 插件）** — 最终 JAR 中 luaj 的类被重定位到 `com.debugbridge.luaj.*`，即使其他模组也内嵌了 `org.luaj.*`，两者不会冲突。

### 技术实现

**Forge 1.20.1** (`mod/forge-1.20.1/build.gradle.kts`):
- 使用 `com.github.johnrengelman.shadow` 插件进行包重定位。
- `relocate("org.luaj", "com.debugbridge.luaj")` 在字节码级别将所有 `org.luaj` 引用改为 `com.debugbridge.luaj`。
- `dependencies { include(dependency("org.luaj:luaj-jse:.*")) }` 确保只打包 luaj 和 websocket，不包含 Forge/Minecraft 运行时环境。
- 精确的 `exclude` 规则排除游戏资源（`net/minecraft/**`、`net/minecraftforge/**` 等）。

**NeoForge 1.21.1** (`mod/neoforge-1.21.1/build.gradle.kts`):
- 使用 `com.gradleup.shadow` 插件进行包重定位。
- 同样的 `relocate` 和 `include` 策略。
- 额外排除 `assets/**`、`data/**` 等游戏资源。
- 因 NeoForge moddev 不兼容 module path，不添加 `module-info.java`。

### 验证

最终 JAR 结构应满足：
- `com/debugbridge/luaj/` — 重定位后的 luaj 类
- `org/luaj/` — 0 个原始 luaj 类
- `net/minecraft/`、`net/minecraftforge/` 或 `net/neoforged/` — 0 个游戏类

```powershell
# Forge 1.20.1
.\gradlew.bat :forge-1.20.1:shadowJar --console=plain
python -c "import zipfile; z=zipfile.ZipFile(r'forge-1.20.1\build\libs\debugbridge-1.20.1-forge-%VERSION%.jar'); print(sum(1 for n in z.namelist() if 'com/debugbridge/luaj' in n), 'relocated luaj classes')"

# NeoForge 1.21.1
.\gradlew.bat :neoforge-1.21.1:shadowJar --console=plain
python -c "import zipfile; z=zipfile.ZipFile(r'neoforge-1.21.1\build\libs\debugbridge-1.21.1-neoforge-%VERSION%.jar'); print(sum(1 for n in z.namelist() if 'com/debugbridge/luaj' in n), 'relocated luaj classes')"
```

## Verification

```powershell
.\gradlew.bat :core:test --console=plain
```
