# forge/1.20.1 — 开发计划

## 状态
- ✅ **编译通过** (Gradle 8.10.2 + ForgeGradle 6.0 + Forge 1.20.1-47.2.0)
- ✅ **JAR 生成** (`debugbridge-1.20.1-forge-1.1.0.jar`, 含依赖)
- ✅ **runClient dev 模式运行正常**（extractBridgeLibs 注入外部库）
- ✅ **WebSocket 端口 9876 已监听**
- ✅ **全部端点验证通过**
- ✅ **Lua execute / java.import / java.describe 正常**
- ✅ **java.ref() 双向引用** — `java.ref(obj) → refId` 和 `java.ref(refId) → obj`
- ✅ **java.find() 映射搜索** — ForgeSearchResolver 下载 ProGuard 映射（7436 个类）

## 构建 & 运行

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
cd mod
.\gradlew.bat :forge-1.20.1:runClient --console=plain
```

## 模块结构（共用 settings.gradle.kts）

```
mod/
├── settings.gradle.kts    ← 共用：包含 :core, :forge-1.20.1 (Gradle<9), :neoforge-1.21.1
├── build.gradle.kts       ← 共用：Spotless + 通用仓库
├── core/                  ← 共用模块，options.release=17
├── forge-1.20.1/          ← ForgeGradle 6.x，Java 17
└── neoforge-1.21.1/       ← NeoForge moddev 插件，仅 Gradle ≥9 时可用
```

## 修复记录

| 问题 | 修复 |
|---|---|
| Mixin 包路径错误（`neoforge1201`→`forge1201`） | `debugbridge.mixins.json` |
| FPS 获取（`mc.fpsString`→`mc.getFps()`） | `DebugBridgeMod.java` |
| 运行时 ClassNotFoundException: WebSocketServer | `extractBridgeLibs` task |
| 类路径依赖传递（core 非子项目） | `implementation(project(":core"))` |
| `java.ref()` 只支持读取 | 新增对象→refId 分支 |
| `java.find()` 返回空 | `ForgeSearchResolver` |

## 已知限制
- JAR 依赖通过 `from()` 手动打包
- 纹理使用反射提取精灵像素（同 1.19 Fabric，无 GPU 管线）
