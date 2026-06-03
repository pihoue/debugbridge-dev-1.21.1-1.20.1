# DebugBridge Mod Notes (NeoForge 1.21.1)

## Project Shape

- `core`: protocol, server, Lua bridge, mapping, snapshots, screenshots, textures, and provider interfaces.
- `neoforge-1.21.1`: Minecraft version-specific adapters for NeoForge 1.21.1.

## Building

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
.\gradlew.bat :neoforge-1.21.1:jar --console=plain
```

## Testing

```powershell
.\gradlew.bat :core:test --console=plain
```

## Key Points

- Uses Mojang names directly (no mapping resolution needed)
- `BlockGlowMixin` hooks into `LevelRenderer.renderLevel`
- Item textures via baked model sprite extraction
