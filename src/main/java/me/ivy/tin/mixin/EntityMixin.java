package me.ivy.tin.mixin;

import me.ivy.tin.RotationUtils;
import me.ivy.tin.Tin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(at = @At("HEAD"),
            method = "changeLookDirection")
    private void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (cursorDeltaX == 0 && cursorDeltaY == 0) {
            return;
        }
        double g = RotationUtils.getMouseSensivityMultiplier();
        if (Tin.getInstance().config.DEBUG) {
            double mouseX = cursorDeltaX/g;
            double mouseY = cursorDeltaY/g;
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("Mouse X: " + mouseX + " Y: " + mouseY));
        }
    }
}
