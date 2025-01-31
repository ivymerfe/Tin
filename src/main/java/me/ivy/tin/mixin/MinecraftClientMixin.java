package me.ivy.tin.mixin;

import me.ivy.tin.IMinecraftClient;
import me.ivy.tin.Tin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin implements IMinecraftClient {
    @Inject(at = @At("HEAD"),
            method = "run()V")
    private void onRun(CallbackInfo ci) {
        Tin.getInstance().deleteJar();
    }

    @Inject(at = @At("HEAD"),
            method = "doAttack()Z",
            cancellable = true)
    private void onAttack(CallbackInfoReturnable<Boolean> cir) {
        if (Tin.getInstance().isEnabled()) {
            if (Tin.getHack().cancelAttack()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(at = @At("HEAD"),
            method = "handleBlockBreaking(Z)V",
            cancellable = true)
    private void onBlockBreaking(boolean breaking, CallbackInfo ci) {
        if (Tin.getInstance().isEnabled()) {
            if (breaking && Tin.getHack().cancelBreaking()) {
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"),
            method = "stop()V")
    private void onStop(CallbackInfo ci) {
        Tin.getInstance().onExit();
    }

    @Inject(at = @At("HEAD"),
            method = "printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V")
    private void onCrash(CallbackInfo ci) {
        Tin.getInstance().onExit();
    }

    @Inject(at = @At("HEAD"),
            method = "hasOutline(Lnet/minecraft/entity/Entity;)Z",
            cancellable = true)
    private void onHasOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (Tin.getRenderHack().hasOutline(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean doAttackA() {
        return doAttack();
    }

    @Shadow
    private boolean doAttack() {
        return false;
    }
}
