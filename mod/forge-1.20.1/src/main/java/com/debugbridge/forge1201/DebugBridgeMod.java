package com.debugbridge.forge1201;

import com.debugbridge.core.block.NearbyBlocksProvider;
import com.debugbridge.core.chat.ChatHistoryProvider;
import com.debugbridge.core.entity.LookedAtEntityProvider;
import com.debugbridge.core.entity.NearbyEntitiesProvider;
import com.debugbridge.core.lifecycle.AbstractDebugBridgeMod;
import com.debugbridge.core.mapping.FabricNamespaceLookup;
import com.debugbridge.core.mapping.MappingCache;
import com.debugbridge.core.mapping.MappingDownloader;
import com.debugbridge.core.mapping.MappingResolver;
import com.debugbridge.core.mapping.ParsedMappings;
import com.debugbridge.core.mapping.ProGuardParser;
import com.debugbridge.core.protocol.dto.SnapshotDto;
import com.debugbridge.core.protocol.dto.SnapshotPlayerDto;
import com.debugbridge.core.protocol.dto.SnapshotTargetDto;
import com.debugbridge.core.protocol.dto.SnapshotVehicleDto;
import com.debugbridge.core.protocol.dto.SnapshotWorldDto;
import com.debugbridge.core.protocol.dto.Vec3Dto;
import com.debugbridge.core.recording.FrameCapturer;
import com.debugbridge.core.screen.ScreenInspectProvider;
import com.debugbridge.core.screenshot.ScreenshotProvider;
import com.debugbridge.core.snapshot.GameStateProvider;
import com.debugbridge.core.texture.ItemTextureProvider;
import java.nio.file.Path;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod("debugbridge")
public class DebugBridgeMod extends AbstractDebugBridgeMod {
    private static final String MC_VERSION = "1.20.1";
    private static DebugBridgeMod INSTANCE;

    public DebugBridgeMod() {
        INSTANCE = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    public static void onClientTick(Minecraft mc) {
        if (INSTANCE != null) INSTANCE.handleTick();
    }

    public static void onRenderFrame(Minecraft mc) {
        if (INSTANCE != null) INSTANCE.handleRenderFrame();
    }

    public static void onClientClose(Minecraft mc) {
        if (INSTANCE != null) INSTANCE.handleClose();
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        initialize();
    }

    @Override
    protected String mcVersion() { return MC_VERSION; }

    @Override
    protected Path configDir() { return FMLPaths.CONFIGDIR.get(); }

    @Override
    protected Path gameDir() { return FMLPaths.GAMEDIR.get(); }

    @Override
    protected FabricNamespaceLookup createNamespaceLookup() { return null; }

    @Override
    protected MappingResolver buildResolver() {
        try {
            String mcVer = mcVersion();
            MappingCache cache = new MappingCache();
            String content;
            if (cache.has(mcVer)) {
                LOG.info("[DebugBridge] Loading cached " + mcVer + " mappings...");
                content = cache.load(mcVer);
            } else {
                LOG.info("[DebugBridge] Downloading " + mcVer + " mappings from Mojang...");
                content = new MappingDownloader().download(mcVer);
                cache.save(mcVer, content);
            }
            ParsedMappings mappings = ProGuardParser.parse(content);
            LOG.info("[DebugBridge] Parsed " + mappings.classes.size() + " classes from mappings.");
            return new ForgeSearchResolver(mcVer, mappings);
        } catch (Exception e) {
            LOG.warning("[DebugBridge] Failed to load mappings: " + e.getMessage());
            return super.buildResolver();
        }
    }

    @Override
    protected void submitToGameThread(Runnable task) { Minecraft.getInstance().execute(task); }

    @Override
    protected GameStateProvider createStateProvider() { return new Minecraft1201StateProvider(); }

    @Override
    protected ScreenshotProvider createScreenshotProvider() { return new Minecraft1201ScreenshotProvider(); }

    @Override
    protected FrameCapturer createFrameCapturer() { return new Minecraft1201FrameCapturer(); }

    @Override
    protected ItemTextureProvider createTextureProvider() { return new Minecraft1201ItemTextureProvider(); }

    @Override
    protected NearbyEntitiesProvider createEntitiesProvider() { return new Minecraft1201NearbyEntitiesProvider(); }

    @Override
    protected NearbyBlocksProvider createBlocksProvider() { return new Minecraft1201NearbyBlocksProvider(); }

    @Override
    protected LookedAtEntityProvider createLookedAtEntityProvider() { return new Minecraft1201LookedAtEntityProvider(); }

    @Override
    protected ChatHistoryProvider createChatHistoryProvider() { return new Minecraft1201ChatHistoryProvider(); }

    @Override
    protected ScreenInspectProvider createScreenInspectProvider() { return new Minecraft1201ScreenInspectProvider(); }

    @Override
    protected boolean displayPlayerError(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        mc.player.displayClientMessage(Component.literal("[DebugBridge] " + message).withStyle(s -> s.withColor(0xFF5555)), false);
        return true;
    }

    @Override
    protected boolean displayPlayerInfo(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        mc.player.displayClientMessage(Component.literal("[DebugBridge] " + message).withStyle(s -> s.withColor(0x55FF55)), false);
        return true;
    }

    @Override
    protected boolean canShowWarningScreen() {
        Minecraft mc = Minecraft.getInstance();
        return mc.screen == null && mc.getOverlay() == null;
    }

    @Override
    protected void showWarningScreen(Consumer<Boolean> onResult) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new DeveloperWarningScreen(config, accepted -> { mc.setScreen(null); onResult.accept(accepted); }));
    }

    private static class Minecraft1201StateProvider implements GameStateProvider {
        @Override
        public SnapshotDto captureSnapshot() {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            SnapshotDto snap = new SnapshotDto();
            if (player != null) {
                SnapshotPlayerDto p = new SnapshotPlayerDto();
                p.name = player.getName().getString();
                p.x = player.getX(); p.y = player.getY(); p.z = player.getZ();
                p.yaw = player.getYRot(); p.pitch = player.getXRot();
                p.hotbarSlot = player.getInventory().selected;
                p.health = player.getHealth(); p.maxHealth = player.getMaxHealth();
                p.food = player.getFoodData().getFoodLevel();
                p.saturation = player.getFoodData().getSaturationLevel();
                p.dimension = player.level().dimension().location().toString();
                p.biome = "";
                Vec3 vel = player.getDeltaMovement();
                p.velocity = new Vec3Dto(vel.x, vel.y, vel.z);
                Vec3 look = player.getLookAngle();
                p.look = new Vec3Dto(look.x, look.y, look.z);
                Entity vehicle = player.getVehicle();
                if (vehicle != null) {
                    SnapshotVehicleDto v = new SnapshotVehicleDto();
                    v.entityId = vehicle.getId(); v.type = vehicle.getClass().getName();
                    p.vehicle = v;
                }
                snap.player = p;
            }
            HitResult hit = mc.hitResult;
            if (hit != null && hit.getType() != HitResult.Type.MISS) {
                SnapshotTargetDto t = new SnapshotTargetDto();
                t.type = hit.getType().name().toLowerCase();
                if (hit instanceof BlockHitResult bhr) {
                    BlockPos pos = bhr.getBlockPos();
                    t.x = pos.getX(); t.y = pos.getY(); t.z = pos.getZ();
                    t.face = bhr.getDirection().name().toLowerCase();
                } else if (hit instanceof EntityHitResult ehr) {
                    t.entityId = ehr.getEntity().getId();
                    t.entityType = ehr.getEntity().getClass().getName();
                }
                snap.target = t;
            }
            if (mc.level != null) {
                SnapshotWorldDto w = new SnapshotWorldDto();
                w.dayTime = mc.level.getDayTime();
                w.isRaining = mc.level.isRaining();
                w.isThundering = mc.level.isThundering();
                snap.world = w;
            }
            snap.fps = mc.getFps();
            snap.version = MC_VERSION;
            return snap;
        }
    }
}
