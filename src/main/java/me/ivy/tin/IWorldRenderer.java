package me.ivy.tin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

import java.util.List;

public interface IWorldRenderer {
    void renderEntitiesA(MatrixStack matrices,
                         VertexConsumerProvider.Immediate immediate,
                         Camera camera,
                         RenderTickCounter tickCounter,
                         List<Entity> entities);
}
