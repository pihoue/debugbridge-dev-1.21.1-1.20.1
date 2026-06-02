# Handoff: DebugBridge NeoForge 1.20.1

## 当前状态

NeoForge 1.20.1 mod (`net.neoforged.gradle.userdev 7.0.183`, NeoForge `20.4.237`) 编译通过，JAR 已生成。**但 JAR 缺少 jarJar 依赖（仅 58KB），需要修复打包配置。**

## 项目布局

```
mod/
  core/                    ← 加载器无关的核心逻辑（BridgeServer, Lua 运行时, Provider 接口, DTO, 映射解析）
  neoforge-1.20.1/         ← NeoForge 1.20.1 模块（本分支）
```

> gradle-wrapper.properties 已固定为 **8.10.2** (userdev 7.x 不兼容 Gradle 9.x)

## 架构速览

- `DebugBridgeMod.java` (@Mod) → extends `AbstractDebugBridgeMod` (core)
- `handleTick()` 由 mixin (`MinecraftClientMixin`) 在每个客户端 tick 末尾调用
- 10 个 Provider + 1 个 `NeoForgeMappingResolver` 在 `DebugBridgeMod` 中实例化
- 1.20.1 NeoForge 运行时使用 **SRG 名称**（`func_XXXX`/`field_XXXX`）
  - `createNamespaceLookup()` 返回 null → `buildResolver()` 被重写为使用 `NeoForgeMappingResolver`
  - `NeoForgeMappingResolver` 下载 Mojang ProGuard 映射 → 解析为 SRG→Mojang 双向查找表
  - **未经验证**：ProGuard 映射中的 obfuscated 名是否与 NeoForge SRG 名一致
  
## 已知问题（关键）

1. **jarJar 依赖未打包** — JAR 仅 58KB，缺少 core + luaj + websocket + gson。userdev 7.x 的 `jarJar` 配置未生效，需要手动配置 Jar 任务的 `from()`。
2. **NeoForgeMappingResolver 映射准确性** — 如果 ProGuard 映射与 SRG 不匹配，`ChatHistoryProvider` 的反射字段查找会失败。
3. **`Minecraft.close()` 可能存在/不存在** — 需验证。如果不存在则移除该 mixin。
4. **`level.entitiesForRendering()` 性能** — 1.20.1 中需确认该方法是否存在/可用。

## 下一步（详见 `DEV_PLAN-1.20.1.md`）

### P0
1. 修复 jarJar 依赖打包（手动配置 Jar 任务）
2. 验证 NeoForgeMappingResolver 映射准确性（运行时发送 `execute` 脚本检查类名）
3. 部署 JAR 到 MC 1.20.1 NeoForge 实例测试

## 推荐技能

- `context7-mcp` — 查阅 NeoForge 1.20.1 API 差异
- `diagnose` — 端点故障排查
- `dependency-conflict-resolver` — 如果 jarJar 或 SLF4J 依赖冲突

## 参考文件

| 文件 | 说明 |
|------|------|
| `DEV_PLAN-1.20.1.md` | 完整开发计划（P0/P1/P2） |
| `mod/neoforge-1.20.1/build.gradle.kts` | 需要修复 jarJar 配置 |
| `mod/neoforge-1.20.1/src/main/java/com/debugbridge/neoforge1201/NeoForgeMappingResolver.java` | 需运行验证的映射解析器 |
| `mod/core/src/main/java/com/debugbridge/core/mapping/` | 映射解析基础设施（MappingDownloader, ProGuardParser 等） |
