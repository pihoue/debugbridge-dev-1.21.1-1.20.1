package com.debugbridge.neoforge1201.mixin;

import com.debugbridge.core.block.ClientBlockGlowManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class BlockGlowMixin {

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Inject(
            method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V",
            at = @At("TAIL"))
    private void debugbridge$renderBlockGlow(
            PoseStack poseStack,
            float partialTicks,
            long finishTimeNano,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f projectionMatrix,
            CallbackInfo ci) {
        var glowing = ClientBlockGlowManager.snapshot();
        if (glowing.isEmpty()) return;

        Vec3 cam = camera.getPosition();
        MultiBufferSource.BufferSource buffers = renderBuffers.bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        for (var p : glowing) {
            AABB box = new AABB(p.x(), p.y(), p.z(), p.x() + 1.0, p.y() + 1.0, p.z() + 1.0);
            LevelRenderer.renderLineBox(poseStack, lines, box, 1.0f, 1.0f, 0.0f, 1.0f);
        }

        poseStack.popPose();
        buffers.endBatch(RenderType.lines());
    }
}
