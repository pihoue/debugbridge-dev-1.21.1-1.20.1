package com.debugbridge.forge1201.mixin;

import com.debugbridge.forge1201.DebugBridgeMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        DebugBridgeMod.onClientTick(Minecraft.getInstance());
    }

    @Inject(method = "runTick", at = @At("TAIL"))
    private void onRenderFrame(CallbackInfo ci) {
        DebugBridgeMod.onRenderFrame(Minecraft.getInstance());
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        DebugBridgeMod.onClientClose(Minecraft.getInstance());
    }
}
