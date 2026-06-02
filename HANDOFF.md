# Handoff: DebugBridge NeoForge 1.21.1

## 当前状态

NeoForge 1.21.1 mod (`net.neoforged.moddev 2.0.72`, NeoForge `21.1.87`) 编译通过，JAR 已生成。mod 加载后应监听 localhost:9876 提供 WebSocket 调试接口。

## 项目布局

```
mod/
  core/                    ← 加载器无关的核心逻辑（BridgeServer, Lua 运行时, Provider 接口, DTO, 映射解析）
  neoforge-1.21.1/         ← NeoForge 1.21.1 模块（本分支）
  fabric-1.19/             ← 遗留（保留）
  fabric-1.21.11/          ← 遗留（保留）
  fabric-26.2-dev/         ← 遗留（保留）
```

## 架构速览

- `DebugBridgeMod.java` (@Mod) → extends `AbstractDebugBridgeMod` (core)
- `handleTick()` 由 mixin (`MinecraftClientMixin`) 在每个客户端 tick 末尾调用
- 10 个 Provider 接口（如 `GameStateProvider`、`NearbyEntitiesProvider`）由版本特定模块在 `DebugBridgeMod` 中实例化
- `BridgeServer` (core) 处理 switch-case 的 14 种请求类型
- 运行时名称使用 Mojang 名称（`createNamespaceLookup()` 返回 null → `PassthroughResolver`）
- BlockGlow 通过 `BlockGlowMixin` (Mixin into `LevelRenderer.renderLevel`) 实现
- 物品纹理通过 `ItemRenderer.getModel()` + baked model sprite 像素提取实现

## 下一步（详见 `DEV_PLAN-1.21.1.md`）

### P0 — 部署测试
1. 将 JAR 放入 MC 1.21.1 NeoForge 实例的 `mods/`
2. 确认启动无报错，WebSocket 端口 9876 监听
3. 运行 `tools/smoke-test.mjs --port 9876` 验证每个端点

### P0 — 端点验证
所有 14 个端点需逐一验证（snapshot, nearbyEntities, entityDetails, nearbyBlocks, blockDetails, lookedAtEntity, chatHistory, screenInspect, screenshot, getItemTexture, execute, status, setEntityGlow, setBlockGlow/clearBlockGlow）

## 推荐技能

- `context7-mcp` — 如果需查阅 NeoForge API 变更
- `diagnose` — 如果端点返回错误需系统排查

## 参考文件

| 文件 | 说明 |
|------|------|
| `DEV_PLAN-1.21.1.md` | 完整开发计划（P0/P1/P2） |
| `mod/AGENTS.md` | Gradle 构建说明 |
| `mod/core/` | 加载器无关核心（无需修改） |
| `mod/neoforge-1.21.1/src/main/java/com/debugbridge/neoforge1211/` | 所有版本特定 Java 源文件 |
| `tools/smoke-test.mjs` | WebSocket 端点自动化测试 |
