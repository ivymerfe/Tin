package me.ivy.tin.mixin;

import me.ivy.tin.Tin;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(at = @At("HEAD"),
            method = "onKey(JIIII)V")
    private void onOnKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        Tin.getHack().onKey(action, key);
    }
}
