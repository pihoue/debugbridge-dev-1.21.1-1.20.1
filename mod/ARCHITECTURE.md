# DebugBridge Mod Architecture (NeoForge 1.21.1)

## Module Boundaries

- `core`: no DebugBridge module dependencies.
- `neoforge-1.21.1`: depends on `core` and on Minecraft/NeoForge APIs.

## Runtime Model

- `BridgeServer` owns the localhost WebSocket protocol.
- `LuaRuntime` executes scripts through the Java bridge and dispatches Minecraft state access onto the game thread.
- Provider interfaces in `core` expose native fast paths for snapshots, screenshots, entities, blocks, screen inspection, chat history, and item textures.
- `neoforge-1.21.1` module registers providers for Minecraft 1.21.1.

## Verification

```powershell
.\gradlew.bat :core:test --console=plain
```
