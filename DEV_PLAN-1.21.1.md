# neoforge/1.21.1 — 开发计划

## 状态
- ✅ 编译通过
- ✅ JAR 生成 (`debugbridge-1.21.1-neoforge-1.1.0.jar`, 911KB, 含 jarJar 依赖)
- ❌ 运行时测试未做

## 构建

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
cd mod
.\gradlew.bat :neoforge-1.21.1:jar --console=plain
```

JAR 输出: `mod/neoforge-1.21.1/build/libs/debugbridge-1.21.1-neoforge-1.1.0.jar`

## 待完成任务

### P0 — 部署测试
- [ ] 将 JAR 放入 Minecraft 1.21.1 NeoForge 实例的 `mods/` 目录
- [ ] 确认游戏启动无报错
- [ ] 确认聊天栏出现 "DebugBridge" 初始化信息
- [ ] 确认 WebSocket 端口 9876 已监听

### P0 — 端点验证 (使用 tools/smoke-test.mjs)
- [ ] `snapshot` — 返回玩家位置/血量/维度
- [ ] `nearbyEntities` — 返回附近实体列表
- [ ] `entityDetails` — 返回实体详情 (装备/乘客/标签)
- [ ] `nearbyBlocks` — 返回附近容器/告示牌
- [ ] `blockDetails` — 返回容器内容/告示牌文字
- [ ] `lookedAtEntity` — 正确检测玩家指向的实体
- [ ] `chatHistory` — 返回最近聊天记录
- [ ] `screenInspect` — 返回当前打开界面
- [ ] `screenshot` — 截屏文件生成
- [ ] `getItemTexture` — 返回物品图标 base64 PNG
- [ ] `execute` — Lua 脚本执行
- [ ] `status` — 返回服务器状态
- [ ] `setEntityGlow` — 实体发光
- [ ] `setBlockGlow` / `clearBlockGlow` — 方块高亮

### P1 — 功能完善
- [ ] 检查 `BlockGlowMixin` 是否正确勾入 `LevelRenderer.renderLevel` (1.21.1 的方法签名可能与 1.19 不同)
- [ ] 验证 `NeoForgeMappingResolver` 不存在(1.21.1 使用 Mojang 名称，无需映射). 如工作异常, `createNamespaceLookup()` 返回 `null` 会自动使用 `PassthroughResolver`
- [ ] 运行 `./gradlew.bat :neoforge-1.21.1:test` 确认测试 (core 测试可能因 JDK 版本有已知失败)
- [ ] 修复 deprecation warnings (不影响功能, 但可清理)

### P2 — 优化
- [ ] 使用 NeoForge 事件总线替代部分 Mixin (可选)
  - `ClientTickEvent.Post` 替代 `Minecraft.tick()` mixin
  - `RenderLevelStageEvent` 替代 `BlockGlowMixin`
- [ ] jarJar 验证: 确认 `core` + `luaj` + `websocket` + `gson` 正确打入 JAR

### 已知限制
- ItemTextureProvider 使用 baked model sprite 提取(非 GPU 渲染), 不支持染色皮革等 tint 效果 (与 1.20.1 版本行为一致)
- `level.entitiesForRendering()` 返回的实体顺序不确定, 排序由 providers 端按距离执行
- BlockGlowMixin 依赖 `LevelRenderer.renderLevel` 的方法签名; 如果更新 NeoForge 版本需同步检查签名变化
