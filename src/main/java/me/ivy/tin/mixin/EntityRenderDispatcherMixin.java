package me.ivy.tin.mixin;

import me.ivy.tin.Tin;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Redirect(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/EntityRenderer;getAndUpdateRenderState(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/entity/state/EntityRenderState;"),
            method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V")
    private EntityRenderState modifyRenderState(EntityRenderer renderer, Entity entity, float tickDelta) {
        EntityRenderState state = renderer.getAndUpdateRenderState(entity, tickDelta);
        if (Tin.getRenderHack().showInvisible() && state instanceof LivingEntityRenderState state2) {
            state2.invisibleToPlayer = false;
        }
        return state;
    }

    @Redirect(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;getBoundingBox()Lnet/minecraft/util/math/Box;"),
            method = "renderHitbox")
    private static Box getHitbox(Entity entity) {
        return Tin.getHack().getExtendedHitbox(entity.getBoundingBox());
    }
}
