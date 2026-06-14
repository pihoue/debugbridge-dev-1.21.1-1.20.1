# DebugBridge Mod Architecture (NeoForge 1.21.1)

## Module Boundaries

- `core`: no DebugBridge module dependencies.
- `neoforge-1.21.1`: depends on `core` and on Minecraft/NeoForge APIs.

## Runtime Model

- `BridgeServer` owns the localhost WebSocket protocol.
- `LuaRuntime` executes scripts through the Java bridge and dispatches Minecraft state access onto the game thread.
- Provider interfaces in `core` expose native fast paths for snapshots, screenshots, entities, blocks, screen inspection, chat history, and item textures.
- `neoforge-1.21.1` module registers providers for Minecraft 1.21.1.

## LuaJ 依赖管理（避免 JPMS/Classpath 冲突）

`org.luaj:luaj-jse:3.0.1` 是 DebugBridge 的嵌入式 Lua 5.2 运行时。为防止与其他同样内嵌 luaj 的模组发生类冲突，采用以下策略：

### 原则

1. **不将 `org.luaj` 包内嵌在源码或资源中** — luaj 仅作为 Maven 依赖引入，不使用 jarJar 直接嵌入。
2. **桥接模块隔离** — 所有 `org.luaj` 的 import 集中在 `core` 模块的 `LuaRuntime`、`JavaBridge`、`MethodCallWrapper` 中，neoforge 模块不直接引用 luaj 类型。
3. **包重定位（Shadow 插件）** — 最终 JAR 中 luaj 的类被重定位到 `com.debugbridge.luaj.*`，即使其他模组也内嵌了 `org.luaj.*`，两者不会冲突。

### 技术实现

- `mod/neoforge-1.21.1/build.gradle.kts` 使用 `com.gradleup.shadow` 插件进行包重定位。
- `relocate("org.luaj", "com.debugbridge.luaj")` 在字节码级别将所有 `org.luaj` 引用改为 `com.debugbridge.luaj`。
- `dependencies { include(dependency("org.luaj:luaj-jse:.*")) }` 确保只打包 luaj 和 websocket，不包含 Minecraft/NeoForge 运行时环境。
- 精确的 `exclude` 规则排除游戏资源（`assets/**`、`data/**`、`net/minecraft/**` 等）。

### 为什么不使用 module-info.java？

NeoForge moddev 插件在编译时将依赖放在 **classpath** 而非 **module path** 上。添加 `module-info.java` 会使模块成为 JPMS 具名模块，但：

| 问题 | 说明 |
|------|------|
| 无法解析 `requires` | NeoForge 依赖（Mixin、NeoForge Bus、WebSocket 等）都是有 module-info 的具名模块，但 moddev 不将它们置入 module path |
| JDK 模块未声明 | 代码使用了 `java.desktop`、`java.net.http` 等模块，需要额外 `requires` |
| `--add-reads ALL-UNNAMED` 不足 | 只能访问无名模块，无法替代 `requires` 具名模块 |

因此 **不添加 module-info.java**。Shadow 包重定位已从字节码层面消除 `org.luaj` 包冲突，无需 JPMS 模块声明。

### 验证

最终 JAR 结构应满足：
- `com/debugbridge/luaj/` — 354 个重定位后的 luaj 类
- `org/luaj/` — 0 个原始 luaj 类
- `net/minecraft/`、`net/neoforged/` — 0 个游戏类

```powershell
# 构建并检查
.\gradlew.bat :neoforge-1.21.1:shadowJar --console=plain
python -c "import zipfile; z=zipfile.ZipFile(r'neoforge-1.21.1\build\libs\debugbridge-1.21.1-neoforge-%VERSION%.jar'); print(sum(1 for n in z.namelist() if 'com/debugbridge/luaj' in n), 'relocated luaj classes')"
```

## Verification

```powershell
.\gradlew.bat :core:test --console=plain
```
