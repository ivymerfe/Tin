package me.ivy.tin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;

import java.util.concurrent.ThreadLocalRandom;

public class Helper {
    public static int randomInt(int origin, int deviation, int min, int max) {
        return ThreadLocalRandom.current().nextInt(Math.max(min, origin-deviation), Math.min(max, origin+deviation));
    }

    public static double randomDouble(double origin, double deviation, double min, double max) {
        return ThreadLocalRandom.current().nextDouble(Math.max(min, origin-deviation), Math.min(max, origin+deviation));
    }

    public static double smoothFunction(double x, double alpha, double beta, double min, double max) {
        double scale = Math.pow((Math.PI*2)/alpha, 1/beta);
        // a + b = min, b - a = max
        double b = (min+max)/2;
        double a = b-max;
        return a*Math.cos(alpha*Math.pow(x*scale, beta)) + b;
    }

    public static boolean shouldWaitForCrit(double minVelocity) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return false;
        }
        return MinecraftClient.getInstance().options.jumpKey.isPressed()
                && (player.isOnGround() || player.getVelocity().y > minVelocity)
                && !player.isClimbing()
                && !player.isTouchingWater()
                && !player.hasStatusEffect(StatusEffects.BLINDNESS)
                && !player.hasVehicle();
    }

    public static int getRandomAttackCooldown(int dev, int min, int max) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) {
            return 0;
        }
        if (Float.isInfinite(player.getAttackCooldownProgressPerTick())) {
            return 1000;
        }
        int fullProgressTicks = Math.round(player.getAttackCooldownProgressPerTick());
        return randomInt(fullProgressTicks, dev, min, max);
    }

    public static int getRemainingTicks() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) {
            return 0;
        }
        if (Float.isInfinite(player.getAttackCooldownProgressPerTick())) {
            return 1000;
        }
        return Math.round((1-player.getAttackCooldownProgress(0))*player.getAttackCooldownProgressPerTick());
    }
}
