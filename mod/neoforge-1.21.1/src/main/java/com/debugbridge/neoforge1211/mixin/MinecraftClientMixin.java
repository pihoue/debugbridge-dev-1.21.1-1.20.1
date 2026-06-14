package com.debugbridge.neoforge1211.mixin;

import com.debugbridge.neoforge1211.DebugBridgeMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        DebugBridgeMod.onClientClose((Minecraft) (Object) this);
    }
}
