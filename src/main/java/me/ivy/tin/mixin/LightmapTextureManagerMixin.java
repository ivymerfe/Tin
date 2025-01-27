package me.ivy.tin.mixin;

import me.ivy.tin.Tin;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @Redirect(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;",
            ordinal = 1),
            method = "update(F)V")
    private Object onGetGamma(SimpleOption<Double> option) {
        return Tin.getRenderHack().getGamma(option.getValue());
    }
}
