# neoforge/1.21.1 — 开发计划

## 最终状态
- ✅ **编译通过** (core + neoforge 单模块)
- ✅ **JAR 生成** (`debugbridge-1.21.1-neoforge-1.1.0.jar`, ~911KB, 含 jarJar 依赖)
- ✅ **runClient dev 模式正常** (JAR 部署到 run/mods + 源码集)
- ✅ **smoke-test 全部 8/8 通过**
- ✅ **WebSocket 端口 9876 已监听**

## 构建 & 运行

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
cd mod
.\gradlew.bat :neoforge-1.21.1:runClient --console=plain
```

JAR 输出: `mod/neoforge-1.21.1/build/libs/debugbridge-1.21.1-neoforge-1.1.0.jar`

## 测试

```powershell
node tools/smoke-test.mjs --port 9876
```

## 关键架构变更

1. **事件总线替代 Mixin** — `ClientTickEvent.Post` / `RenderFrameEvent.Post` 替代 `Minecraft.tick()` / `runTick()` mixin
2. **BridgeServer 重构** — 组合替代继承，WebSocketServer/LuaJ 懒加载
3. **单模块构建** — core 源码通过 `sourceSet` 合并到 neoforge
4. **JAR 部署** — dev 模式自动将 JAR 部署到 `run/mods/`，确保外部库（WebSocket、LuaJ）从 JAR 加载而非模块路径

## 已知限制
- Mixin 在 ModDevGradle dev 模式不可用（JAR 方式部署正常）
- BlockGlowMixin/EntityGlowMixin/MinecraftClientMixin 保留
- `RenderLevelStageEvent` 替代 `BlockGlowMixin`（需要 accessor mixin 访问 renderBuffers，暂缓）
