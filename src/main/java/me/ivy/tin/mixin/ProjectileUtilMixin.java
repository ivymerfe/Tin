package me.ivy.tin.mixin;

import me.ivy.tin.Tin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {
    @Redirect(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;getBoundingBox()Lnet/minecraft/util/math/Box;"),
            method = "raycast")
    private static Box getHitbox(Entity entity) {
        if (!entity.isAlive()) {
            if (Tin.getInstance().isEnabled() && Tin.getInstance().config.deadFilter) {
                return entity.getBoundingBox().expand(-100); // I know
            }
        }
        return Tin.getHack().getExtendedHitbox(entity.getBoundingBox());
    }

    // We actually send hit pos on entity interact
    // So it has to be on real hitbox
    @Inject(method = "raycast", at = @At("RETURN"), cancellable = true)
    private static void clampResult(CallbackInfoReturnable<EntityHitResult> cir) {
        EntityHitResult result = cir.getReturnValue();
        if (result != null) {
            Vec3d pos = result.getPos();
            Entity entity = result.getEntity();
            Box hitbox = entity.getBoundingBox();
            // TODO - randomize??
            double posX = MathHelper.clamp(pos.x, hitbox.minX, hitbox.maxX);
            double posY = MathHelper.clamp(pos.y, hitbox.minY, hitbox.maxY);
            double posZ = MathHelper.clamp(pos.z, hitbox.minZ, hitbox.maxZ);
            Vec3d newPos = new Vec3d(posX, posY, posZ);
            cir.setReturnValue(new EntityHitResult(entity, newPos));
        }
    }
}
