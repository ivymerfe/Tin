package me.ivy.tin.mixin;

import me.ivy.tin.Tin;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Shadow
    public float nauseaIntensity;
    @Shadow
    public float prevNauseaIntensity;

    @Inject(at = @At("TAIL"),
            method = "tickNausea(Z)V")
    private void onTickNausea(boolean fromPortalEffect, CallbackInfo ci) {
        if (Tin.getRenderHack().noNausea()) {
            this.nauseaIntensity = 0;
        }
    }
}
