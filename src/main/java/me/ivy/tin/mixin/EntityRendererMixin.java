package me.ivy.tin.mixin;

import me.ivy.tin.Tin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V"),
            method = "renderLabelIfPresent")
    private void scaleLabels(EntityRenderState state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (Tin.getRenderHack().scaleLabels() && state.nameLabelPos != null) {
            double d = Math.sqrt(state.squaredDistanceToCamera);
            float a = (float) Math.max(1, d/8);
            matrices.translate(0, 0.25*(a-1), 0);
            matrices.scale(a, a, a);
        }
    }
}
