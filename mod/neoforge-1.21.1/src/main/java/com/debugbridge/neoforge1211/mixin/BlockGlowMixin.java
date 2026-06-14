package com.debugbridge.neoforge1211.mixin;

import com.debugbridge.core.block.ClientBlockGlowManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class BlockGlowMixin {

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void debugbridge$renderBlockGlow(
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f projectionMatrix,
            Matrix4f modelViewMatrix,
            CallbackInfo ci) {
        var glowing = ClientBlockGlowManager.snapshot();
        if (glowing.isEmpty()) return;

        Vec3 cam = camera.getPosition();
        PoseStack poseStack = new PoseStack();
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        MultiBufferSource.BufferSource buffers = renderBuffers.bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        for (var p : glowing) {
            AABB box = new AABB(p.x(), p.y(), p.z(), p.x() + 1.0, p.y() + 1.0, p.z() + 1.0);
            LevelRenderer.renderLineBox(poseStack, lines, box, 1.0f, 1.0f, 0.0f, 1.0f);
        }

        buffers.endBatch(RenderType.lines());
    }
}
