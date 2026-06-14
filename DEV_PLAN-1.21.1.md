# neoforge/1.21.1 — 开发计划

## 最终状态
- ✅ **编译通过** (core + neoforge)
- ✅ **JAR 生成** (`debugbridge-1.21.1-neoforge-1.1.0.jar`)
- ✅ **runClient dev 模式正常** (JAR 部署到 run/mods + 源码集)
- ✅ **WebSocket 端口 9876 已监听**
- ✅ **全部端点验证通过**: snapshot, status, nearbyEntities, lookedAtEntity, chatHistory, screenInspect, screenshot, getItemTextureById
- ✅ **Lua execute 修复** — `this.lua is null` 已修复
- ✅ **java.ref() 双向引用** — `java.ref(obj) → refId` 和 `java.ref(refId) → obj`
- ✅ **java.find() 映射搜索** — NeoForgeSearchResolver 下载 ProGuard 映射

## 构建 & 运行

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
cd mod
.\gradlew.bat :neoforge-1.21.1:runClient --console=plain
```

JAR 输出: `mod/neoforge-1.21.1/build/libs/debugbridge-1.21.1-neoforge-1.1.0.jar`

## 模块结构（共用 settings.gradle.kts）

```
mod/
├── settings.gradle.kts    ← 共用：包含 :core, :forge-1.20.1 (Gradle<9), :neoforge-1.21.1
├── build.gradle.kts       ← 共用：Spotless + 通用仓库
├── core/                  ← 共用模块，options.release=17
├── forge-1.20.1/          ← ForgeGradle 6.x，仅 Gradle <9 时可用
└── neoforge-1.21.1/       ← NeoForge moddev 插件，Java 21
```

## 已知限制
- `java.find()` 依赖 ProGuard 映射数据，仅 forge/1.20.1 和 neoforge/1.21.1 支持
- 无 `MinecraftMixin`（NeoForge 事件总线替代）
- BlockGlow 通过 Mixin 实现（`BlockGlowMixin`）
