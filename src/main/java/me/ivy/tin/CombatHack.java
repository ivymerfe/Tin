package me.ivy.tin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

public class CombatHack {
    final Tin TIN;

    public CombatHack(Tin tin) {
        this.TIN = tin;
    }

    Entity target;
    int enableButtonDoubleclickTicks = 0;
    int nextAttackTicks = 0;
    boolean extendHitboxes = false;
    int smoothTicks = 0;
    int smoothMaxTicks = 1;
    double alpha = 1;
    double beta = 1;
    double min = 0.5;
    double max = 2;
    double gamma = 0.75;
    boolean missSignal = false;
    int targetLostTicks = 0;
    double noDragSpeedMult = 1;
    double critMinVelocity = -0.1;

    public void onKey(int action, int key) {
        if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_F12) {
            if (enableButtonDoubleclickTicks > 0) {
                if (TIN.isEnabled()) {
                    nextAttackTicks = 0;
                    enableButtonDoubleclickTicks = 0;
                }
                TIN.setEnabled(!TIN.isEnabled());
            } else {
                enableButtonDoubleclickTicks = 5;
            }
        }
        if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_R) {
            if (TIN.isEnabled() && MinecraftClient.getInstance().options.useKey.isPressed()) {
                extendHitboxes = !extendHitboxes;
                if (TIN.config.showHitboxes) {
                    MinecraftClient.getInstance().getEntityRenderDispatcher().setRenderHitboxes(extendHitboxes);
                }
            }
        }
    }

    public Box getExtendedHitbox(Box box) {
        if (!TIN.isEnabled() || !extendHitboxes) {
            return box;
        }
        return box.expand(TIN.config.getHorizontalHitbox(), TIN.config.getVerticalHitbox(), TIN.config.getHorizontalHitbox());
    }

    public boolean cancelAttack() {  // true = cancel
        if (!missSignal && MinecraftClient.getInstance().targetedEntity == null && target != null) {
            return true;
        }
        smoothTicks = 0;
        nextAttackTicks = randomAttackCooldown();
        updateParameters();
        missSignal = false;
        return false;
    }

    private void updateParameters() {
        smoothMaxTicks = Helper.randomInt(TIN.config.getSmoothTicks(), 4, 5, 200);
        alpha = Helper.randomDouble(2, 0.5, 1, 3);
        beta = Helper.randomDouble(1.6, 0.3, 1.3, 1.8);
        min = Helper.randomDouble(0.5, 0.25, 0.25, 0.75);
        max = Helper.randomDouble(1.5, 0.3, 1, 2);
        gamma = Helper.randomDouble(1, 0.4, 0.5, 2);
        critMinVelocity = Helper.randomDouble(-0.1, 0.1, -0.2, 0);
    }

    public boolean cancelBreaking() {
        return target != null || targetLostTicks > 0;
    }

    private Entity findTarget() {
        MinecraftClient mc = MinecraftClient.getInstance();
        Entity camera = mc.getCameraEntity();
        if (camera == null) {
            return null;
        }
        Vec3d cameraPos = camera.getCameraPosVec(0);
        Vec3d rotation = camera.getRotationVec(0);
        Box box = camera.getBoundingBox().expand(10,6,10);
        double aimAngle = TIN.config.getAimbotAngle();
        return mc.world.getOtherEntities(camera, box)
                .stream()
                .filter(e -> e instanceof LivingEntity && e.isAlive())
                .filter(e -> RotationUtils.angleXZ(cameraPos,rotation,e) < aimAngle)
                .min(Comparator.comparingDouble(e -> RotationUtils.angleXZ(cameraPos,rotation,e)))
                .orElse(null);
    }

    public void onTick() {
        if (enableButtonDoubleclickTicks > 0) {
            enableButtonDoubleclickTicks--;
        }
        if (!TIN.isEnabled()) {
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.targetedEntity != null) {
            noDragSpeedMult = TIN.config.getNoDragBase();
        } else {
            noDragSpeedMult = Math.min(1, noDragSpeedMult*TIN.config.getNoDragMult());
        }
        if (TIN.config.aimbotEnabled) {
            boolean hadTarget = target != null;
            target = findTarget();
            smoothTicks = (smoothTicks+1)%smoothMaxTicks;
            if (hadTarget && target == null) {
                targetLostTicks = TIN.config.getBlockBreakingDelay();
            }
            if (targetLostTicks > 0) {
                targetLostTicks--;
            }
        }
        if (TIN.config.autoAttack) {
            updateAttackTicks();
            maybeMiss();
            maybeAttack();
        }
    }

    private int randomAttackCooldown() {
        return Helper.getRandomAttackCooldown(3, 6, 1000);
    }

    private void updateAttackTicks() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.targetedEntity != null) {
            int randomAttackTicks = randomAttackCooldown();
            if (nextAttackTicks > 0) {
                nextAttackTicks = Math.min(nextAttackTicks-1, randomAttackTicks);
            }
        } else {
            nextAttackTicks = Helper.randomInt(Helper.getRemainingTicks()+1, 3, 2, 1000);
        }
    }

    private void maybeMiss() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (target != null && mc.targetedEntity == null && !mc.player.isUsingItem() && ThreadLocalRandom.current().nextDouble() < TIN.config.getMissChance()) {
            if (mc.player.getAttackCooldownProgress(0) < 0.2) {
                return;
            }
            if (TIN.config.isAimbotButtonPressed()) {
                missSignal = true;
                ((IMinecraftClient) mc).doAttackA();
                nextAttackTicks = randomAttackCooldown();
            }
        }
    }

    private void maybeAttack() {
        MinecraftClient mc = MinecraftClient.getInstance();
        Entity targetedEntity = mc.targetedEntity;
        if (targetedEntity != null && nextAttackTicks == 0 && (!TIN.config.waitForCrits || !Helper.shouldWaitForCrit(critMinVelocity))) {
            if (targetedEntity instanceof LivingEntity livingEntity) {
                if (livingEntity.hurtTime > 0 || !livingEntity.isAlive()) {
                    return;
                }
            }
            if (mc.player.isUsingItem()) {
                nextAttackTicks = ThreadLocalRandom.current().nextInt(1, 3);
                return;
            }
            if (TIN.config.isAimbotButtonPressed()) {
                ((IMinecraftClient)mc).doAttackA();
                nextAttackTicks = randomAttackCooldown();
            }
        }
    }

    public void onRender(float tickDelta) {
        if (target == null) {
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!TIN.config.isAimbotButtonPressed()) {
            return;
        }
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null) {
            return;
        }
        if (cameraEntity.squaredDistanceTo(target) > 25) {
            return;
        }
        Vec3d cameraPos = cameraEntity.getCameraPosVec(tickDelta);
        Vec3d rotationVec = cameraEntity.getRotationVec(tickDelta);
        Box targetBox = RotationUtils.getLerpedBox(target, tickDelta).contract(0.2, 0, 0.2);

        Vec3d targetVec = GeometryUtils.mcLineBoxQuery(targetBox, cameraPos, rotationVec);
        double neededYaw = RotationUtils.getNeededYaw(cameraPos, targetVec);
        double neededPitch = RotationUtils.getNeededPitch(cameraPos, targetVec);
        double yaw = cameraEntity.getYaw(tickDelta);
        double pitch = cameraEntity.getPitch(tickDelta);
        double yawDelta = MathHelper.wrapDegrees(neededYaw-yaw);
        double pitchDelta = neededPitch-pitch;

        double hSpeed = TIN.config.getHorizontalSpeed()*noDragSpeedMult;
        double vSpeed = TIN.config.getVerticalSpeed()*noDragSpeedMult;
        hSpeed *= Helper.smoothFunction((double) (smoothTicks+tickDelta) / smoothMaxTicks, alpha, beta, min, max);
        vSpeed *= Helper.smoothFunction((double) (smoothTicks+tickDelta) / smoothMaxTicks, alpha*gamma, beta, min, max);
        yawDelta = MathHelper.clamp(yawDelta, -hSpeed, hSpeed);
        pitchDelta = MathHelper.clamp(pitchDelta, -vSpeed, vSpeed);

        double cursorDeltaX = RotationUtils.angleToCursorDelta(yawDelta);
        double cursorDeltaY = RotationUtils.angleToCursorDelta(pitchDelta);
        cameraEntity.changeLookDirection(cursorDeltaX, cursorDeltaY);
    }
}
