# neoforge/1.20.1 — 开发计划

## 状态
- ✅ 编译通过 (Gradle 8.10.2 + NeoGradle userdev 7.0.183)
- ✅ JAR 生成 (`debugbridge-1.20.1-neoforge-1.1.0.jar`, 650KB, 含 jarJar 依赖)
- ✅ IDEA 运行配置已添加 (Build/Run)
- ❌ 运行时测试未做

## 构建

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
cd mod
.\gradlew.bat :neoforge-1.20.1:jar --console=plain
```

IDEA: Run Configuration → Build NeoForge 1.20.1 JAR

## 待完成任务

### P0 — 映射解析验证
- [ ] `NeoForgeMappingResolver` 使用 Mojang ProGuard 映射 → 确认运行时名称匹配
- [ ] 如果映射不匹配, 使用 `PassthroughResolver` (createNamespaceLookup 返回 null)

### P0 — 部署测试
- [ ] 将 JAR 放入 Minecraft 1.20.1 NeoForge 实例的 `mods/` 目录
- [ ] 确认游戏启动无报错
- [ ] 确认 WebSocket 端口 9876 已监听
- [ ] 运行 `node tools/smoke-test.mjs --port 9876`

### P1 — 功能完善
- [ ] `MinecraftClientMixin.close()` — 验证 `Minecraft.close()` 在 1.20.1 是否存在
- [ ] FPS 获取: 当前使用 `mc.fpsString` 解析 vs `Debug.getFPS()`
- [ ] 运行 `.\gradlew.bat :core:test` 确认 core 测试通过
- [ ] 修复 deprecation warnings

### P2 — 优化
- [ ] 使用 NeoForge 事件总线替代 Mixin
- [ ] BlockGlowMixin 验证 1.20.1 方法签名
- [ ] 清理 `fabric-*` 目录
- [ ] IDEA 配置需要重新导入 Gradle 项目

### 已知限制
- jarJar 通过手动 `from()` 合并实现 (NeoGradle userdev 7.x 的 jarJar 不自动包含)
- ItemTextureProvider 使用 baked model sprite 提取, 不支持染色效果
