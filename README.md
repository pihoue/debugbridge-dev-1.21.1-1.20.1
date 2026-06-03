# DebugBridge — NeoForge 1.21.1

A NeoForge client mod for Minecraft 1.21.1 that exposes game state over a local WebSocket server for AI-assisted debugging and development.

## What It Does

Runs a localhost-only WebSocket server (default port 9876, scans 9876–9885) inside Minecraft. External tools — CLI scripts, MCP clients like Claude Code — can inspect and interact with the running game through native endpoints or Lua execution.

### Endpoints

| Endpoint | What it returns |
|---|---|
| `snapshot` | Player position, health, food, dimension, gamemode, time, weather |
| `nearbyEntities` | Entities within range: type, position, equipment, distance |
| `entityDetails` | Full entity info: equipment slots, vehicle, passengers, attributes |
| `lookedAtEntity` | The entity the player is aiming at (raycast) |
| `nearbyBlocks` | Block-entities within range: signs, chests, banners, beacons, etc. |
| `blockDetails` | Block-entity contents: sign lines, chest inventory, skull profile |
| `screenInspect` | Current open screen/gui: type, title, container slots |
| `chatHistory` | Recent client-side chat messages |
| `screenshot` | Capture the framebuffer as JPEG |
| `getItemTexture` / `getItemTextureById` / `getEntityItemTexture` | Render item icons as PNG |
| `setEntityGlow` / `setBlockGlow` / `clearBlockGlow` | Highlight entities/blocks in-world |
| `search` | Search loaded classes by name pattern |
| `status` | Server health and connection info |
| `execute` | Run Lua 5.2 scripts inside the Minecraft JVM |

## Building

Requires **JDK 21**.

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
cd mod
.\gradlew.bat :neoforge-1.21.1:jar --console=plain
```

JAR output: `mod/neoforge-1.21.1/build/libs/debugbridge-1.21.1-neoforge-1.1.0.jar`

## Testing

```powershell
# Core unit tests
cd mod
.\gradlew.bat :core:test --console=plain

# Smoke test against running mod
node tools/smoke-test.mjs --port 9876
```

## Repo Layout

```
mod/
  core/              — Shared Java: BridgeServer, Lua runtime, provider interfaces, DTOs
  neoforge-1.21.1/   — NeoForge 1.21.1 module (provider impls + mixins)
tools/
  smoke-test.mjs     — WebSocket endpoint test script
```

## Architecture

- `BridgeServer` (core) handles WebSocket connections and dispatches to provider implementations
- Provider interfaces in `core/` are implemented by version-specific code in `neoforge-1.21.1/`
- Runtime names use Mojang names (no mapping needed for 1.21.1 NeoForge)
- Item textures extracted via baked model sprite (not GPU render)

## License

MIT License — see LICENSE file for details.
