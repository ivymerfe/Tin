package me.ivy.tin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class RenderHack {
    private final Tin TIN;

    public RenderHack(Tin tin) {
        this.TIN = tin;
    }

    public boolean showInvisible() {
        return TIN.isEnabled() && TIN.config.showInvisible;
    }

    public boolean scaleLabels() {
        return TIN.isEnabled() && TIN.config.scaleLabels;
    }

    public double getGamma(double base) {
        return (TIN.isEnabled() && TIN.config.fullbright) ? 16.0 : base;
    }

    public boolean noFog() {
        return TIN.isEnabled() && TIN.config.noFog;
    }

    public boolean noOverlays() {
        return TIN.isEnabled() && TIN.config.noOverlays;
    }

    public boolean noNausea() {
        return TIN.isEnabled() && TIN.config.noNausea;
    }

    public double getFireOffset() {
        return TIN.isEnabled() ? TIN.config.fireOffset : 0;
    }

    public Vec3d getHeldItemOffset() {
        if (!TIN.isEnabled()) {
            return Vec3d.ZERO;
        }
        return new Vec3d(TIN.config.heldItemOffsetX, TIN.config.heldItemOffsetY, TIN.config.heldItemOffsetZ);
    }

    public int getInvisiblePlayerColor() {
        return 0x99ffffff;
    }

    public boolean hasOutline(Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        return TIN.isEnabled() && TIN.config.playerESP && !InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_R);
    }

    public void renderWithoutDepth(MatrixStack matrixStack, VertexConsumerProvider.Immediate immediate, List<Entity> entities) {
        if (!TIN.isEnabled() || !TIN.config.entityXray) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (!InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_R)) {
            return;
        }
        Camera camera = client.gameRenderer.getCamera();
        RenderTickCounter tickCounter = client.getRenderTickCounter();
        List<Entity> entities2 = entities.stream().filter(e -> e instanceof LivingEntity).toList();
        ((IWorldRenderer)client.worldRenderer).renderEntitiesA(matrixStack, immediate, camera, tickCounter, entities2);
        immediate.draw();
    }
}
