package me.ivy.tin.mixin;

import me.ivy.tin.Tin;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Fog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
    @Inject(at = @At("HEAD"),
            method = "applyFog",
            cancellable = true)
    private static void onApplyFog(CallbackInfoReturnable<Fog> cir) {
        if (Tin.getRenderHack().noFog()) {
            cir.setReturnValue(Fog.DUMMY);
        }
    }
}
