package me.ivy.tin.mixin;

import me.ivy.tin.Tin;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(at = @At("HEAD"),
            method = "renderMiscOverlays",
            cancellable = true)
    private void onRenderMiscOverlays(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Tin.getRenderHack().noOverlays()) {
            ci.cancel();
        }
    }
}
