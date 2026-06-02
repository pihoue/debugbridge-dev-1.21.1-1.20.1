# neoforge/1.20.1 — 开发计划

## 状态
- ✅ 编译通过 (Gradle 8.10.2)
- ✅ JAR 生成 (`debugbridge-1.20.1-neoforge-1.1.0.jar`, 58KB)
- ❌ jarJar 依赖未正确打入 (见已知问题)
- ❌ 运行时测试未做

## 构建

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
cd mod
.\gradlew.bat :neoforge-1.20.1:jar --console=plain
```

JAR 输出: `mod/neoforge-1.20.1/build/libs/debugbridge-1.20.1-neoforge-1.1.0.jar`

## 待完成任务

### P0 — jarJar 依赖修复
- [ ] `neoforge-1.20.1/build.gradle.kts` 中的 `jarJar` 配置在 userdev 7.x 下未正确工作(JAR 只有 58KB，缺少 core + luaj + websocket + gson)
- [ ] 修复方案: userdev 插件需手动配置 Jar 任务:
  ```kotlin
  tasks.jar {
      from({
          configurations.runtimeClasspath.get()
              .filter { it.name.startsWith("core-") || it.name.contains("luaj") || it.name.contains("Java-WebSocket") || it.name.contains("gson") }
              .map { if (it.isDirectory) it else zipTree(it) }
      })
  }
  ```
- [ ] 验证修复后 JAR 至少 900KB+

### P0 — 映射解析验证
- [ ] `NeoForgeMappingResolver` 使用 Mojang ProGuard 映射文件 → 需要确认 ProGuard 中的"obfuscated"名称与 NeoForge SRG 名称是否一致
- [ ] 实际测试: 在游戏中连接 WebSocket, 执行 `execute` Lua 脚本返回 `entity.getClass().getName()` 确认返回的是 SRG 还是 Mojang 名
- [ ] 如果映射不匹配, `ChatHistoryProvider` 的反射字段查找会失败 (`allMessages`, `getMessage`, `addedTime`)
- [ ] 备选方案: 使用 `PassthroughResolver` 并修改 `ChatHistoryProvider` 不使用 MappingResolver (直接硬编码 SRG 名称或通过类型匹配查找字段)

### P0 — 部署测试
- [ ] 将 JAR 放入 Minecraft 1.20.1 NeoForge 实例的 `mods/` 目录
- [ ] 确认游戏启动无报错
- [ ] 确认 WebSocket 端口 9876 已监听

### P0 — 端点验证
- [ ] `snapshot` — 玩家位置/血量/维度 (注意 `dimension.location()` 在 1.20.1 的格式)
- [ ] `nearbyEntities` — 附近实体 (注意: 1.20.1 使用 `Registry.ITEM` → `BuiltInRegistries.ITEM` ✗, 应使用 `net.minecraft.core.Registry.ITEM`)
  
  ⚠️ **重要**: 当前代码使用了 `BuiltInRegistries` 但在这之前是使用 `Registry.ITEM`. 已在编译时替换为 `BuiltInRegistries` 并通过编译。但 `BuiltInRegistries` 是否在 1.20.1 NeoForge 中可用需运行时确认。如果报错, 恢复为 `Registry.ITEM`.
  
- [ ] `screenshot` — 截屏 (使用同步 `Screenshot.takeScreenshot(RenderTarget)`)
- [ ] `getItemTexture` — 物品图标 (使用 baked model sprite 方式)
- [ ] `execute` — Lua 脚本执行
- [ ] `setEntityGlow` / `setBlockGlow` / `clearBlockGlow` — 发光/高亮

### P1 — 功能完善
- [ ] `MinecraftClientMixin.close()` — 验证 `Minecraft.close()` 在 1.20.1 是否存在. 如果不存在, 使用 `Minecraft.stop()` 或移除该 mixin
- [ ] FPS 获取: 当前使用 `mc.fpsString` 解析. 验证 `Debug.getFPS()` 是否存在, 优先使用
- [ ] `level.entitiesForRendering()` 在 1.20.1 中可能存在不同的性能特征; 注意观察
- [ ] 运行 `./gradlew.bat :neoforge-1.20.1:test` 确认 core 测试通过

### P2 — 优化
- [ ] 使用 NeoForge 事件总线替代部分 Mixin (可选)
- [ ] BlockGlowMixin 使用 `LevelRenderer.renderLevel` tail 注入 — 验证 1.20.1 的方法签名与注入点兼容
- [ ] 移除不必要的 `fabric-*` 目录 (它们不影响构建但可能混淆)
- [ ] 更新 `.gitignore` 排除 `build/` 目录

### 已知限制
- ItemTextureProvider 使用 baked model sprite 提取, 不支持染色效果 (与原 Fabric 1.19 版本一致)
- `NeoForgeMappingResolver` 未经验证; 运行时反射字段查找可能失败
- `Minecraft.close()` mixin 在 1.20.1 可能不存在, 需在运行时确认
- 警告: 本分支的 `gradle-wrapper.properties` 已固定为 **8.10.2** (userdev 7.x 不兼容 Gradle 9.x). 升级 Gradle 版本前需要确认 NeoForge userdev 兼容性
