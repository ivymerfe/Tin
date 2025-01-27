package me.ivy.tin.mixin;

import me.ivy.tin.Tin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {
    @Redirect(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;getBoundingBox()Lnet/minecraft/util/math/Box;"),
            method = "raycast")
    private static Box getHitbox(Entity entity) {
        // TODO Zero hitbox if dead

        return Tin.getHack().getExtendedHitbox(entity.getBoundingBox());
    }
}
