# DebugBridge — Handoff

## Current Status

Dual-branch project: **Forge 1.20.1** (ForgeGradle 6.x, Java 17) and **NeoForge 1.21.1** (NeoForge moddev, Java 21).
Both branches compile, run in dev mode, and pass endpoint verification.

## Key Fixes Applied

| Fix | Forge 1.20.1 | NeoForge 1.21.1 |
|-----|-------------|-----------------|
| Mixin package path (`neoforge1201`→`forge1201`) | ✅ | N/A (no Mixin) |
| FPS: `mc.fpsString`→`mc.getFps()` | ✅ | ✅ (native) |
| `java.ref()` bidirectional | ✅ | ✅ |
| `java.find()` search | ✅ (ForgeSearchResolver) | ✅ (NeoForgeSearchResolver) |
| Classpath (dev env) | ✅ extractBridgeLibs | ✅ jarJar |
| Lua runtime NPE | N/A | ✅ |

## Repository Layout

```
mod/
├── settings.gradle.kts    ← Universal (shared between branches)
├── build.gradle.kts       ← Universal (Spotless + repos)
├── core/                  ← Shared, options.release=17
├── forge-1.20.1/          ← ForgeGradle 6.x, Java 17
└── neoforge-1.21.1/       ← NeoForge moddev, Java 21
```

## Running

```powershell
cd mod
.\gradlew.bat :forge-1.20.1:runClient     # Forge 1.20.1
.\gradlew.bat :neoforge-1.21.1:runClient  # NeoForge 1.21.1
```

## Known Limitations

- `java.find()` requires ProGuard mapping data (downloaded on first run).
- Recordings (`record_video`) not tested.
