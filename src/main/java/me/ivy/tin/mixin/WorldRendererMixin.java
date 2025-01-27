package me.ivy.tin.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import me.ivy.tin.IWorldRenderer;
import me.ivy.tin.Tin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.Handle;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin implements IWorldRenderer {
    @Shadow
    private MinecraftClient client;
    @Shadow
    private List<Entity> renderedEntities;
    @Shadow
    private BufferBuilderStorage bufferBuilders;
    @Shadow
    private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(at = @At("HEAD"),
            method = "render")
    private void onRender(ObjectAllocator allocator,
                          RenderTickCounter tickCounter,
                          boolean renderBlockOutline,
                          Camera camera,
                          GameRenderer gameRenderer,
                          Matrix4f positionMatrix,
                          Matrix4f projectionMatrix,
                          CallbackInfo ci) {
        if (Tin.getInstance().isEnabled()) {
            Tin.getHack().onRender(tickCounter.getTickDelta(true));
        }
    }

    @Inject(at = @At("HEAD"),
            method = "tick()V")
    private void onTick(CallbackInfo ci) {
        Tin.getHack().onTick();
    }

    @Inject(at = @At("TAIL"), method = "method_62212") // renderLateDebug lambda
    private void afterRender(Fog fog, Handle<Framebuffer> handle, Vec3d vec, CallbackInfo ci) {
        if (!Tin.getInstance().isEnabled()) {
            return;
        }
        GlStateManager._clearDepth(1.0);
        GlStateManager._clear(256);
        client.getFramebuffer().beginWrite(false);
        MatrixStack matrixStack = new MatrixStack();
        VertexConsumerProvider.Immediate immediate = this.bufferBuilders.getEntityVertexConsumers();
        Tin.getRenderHack().renderWithoutDepth(matrixStack, immediate, this.renderedEntities);
    }

    public void renderEntitiesA(MatrixStack matrices,
                                VertexConsumerProvider.Immediate immediate,
                                Camera camera,
                                RenderTickCounter tickCounter,
                                List<Entity> entities) {
        renderEntities(matrices, immediate, camera, tickCounter, entities);
    }

    @Shadow
    private void renderEntities(MatrixStack matrices,
                                VertexConsumerProvider.Immediate immediate,
                                Camera camera,
                                RenderTickCounter tickCounter,
                                List<Entity> entities) {

    }
}
