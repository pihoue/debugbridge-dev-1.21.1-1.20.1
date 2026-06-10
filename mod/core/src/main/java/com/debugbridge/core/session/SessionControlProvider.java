package com.debugbridge.core.session;

/**
 * Client session control for automation: leave the current world, join a
 * multiplayer server, or quit the client entirely. Lets an external
 * orchestrator (e.g. the MCP server) drive the rebuild → relaunch → rejoin
 * loop without human interaction.
 *
 * <p>Implementations must queue the operation onto the game thread and return
 * promptly — the operation completes asynchronously, after the bridge response
 * has already been sent. Callers observe the outcome by polling
 * {@code snapshot} (player present = in world) and {@code screenInspect}
 * (e.g. a DisconnectedScreen after a failed join).
 *
 * <p>All three endpoints are gated behind {@code session_control_enabled} in
 * the config — they can tear down the user's play session, so they're opt-in
 * the same way {@code runCommand} is.
 */
public interface SessionControlProvider {

    /**
     * Leave the current world/server and return to the title screen.
     * No-op when not in a world.
     */
    void disconnect() throws Exception;

    /**
     * Connect to a multiplayer server, disconnecting from the current world
     * first if needed.
     *
     * @param address             server address as {@code host} or {@code host:port}
     * @param acceptResourcePacks pre-accept the server resource pack so the
     *                            join flow doesn't stall on the confirmation
     *                            prompt
     * @throws IllegalArgumentException if the address fails to parse (thrown
     *                                  synchronously so the request errors
     *                                  instead of failing silently in-game)
     */
    void joinServer(String address, boolean acceptResourcePacks) throws Exception;

    /** Gracefully shut down the Minecraft client (normal stop, not System.exit). */
    void quit() throws Exception;
}
