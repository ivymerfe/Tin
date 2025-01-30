package me.ivy.tin.mixin;

import me.ivy.tin.Tin;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {
    @Redirect(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"),
            method = "renderFireOverlay")
    private static void translate2(MatrixStack matrices, float x, float y, float z) {
        matrices.translate(x, y- Tin.getRenderHack().getFireOffset(), z);
    }
}
