package me.ivy.tin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;

public class CombatHack {
    private final Tin TIN;

    public CombatHack(Tin tin) {
        this.TIN = tin;
    }

    private Entity target;
    private int enableButtonDoubleclickTicks = 0;
    private int nextAttackTicks = 0;
    private boolean extendHitboxes = false;

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
        HitResult crosshairTarget = MinecraftClient.getInstance().crosshairTarget;
        if (crosshairTarget != null && crosshairTarget.getType() != HitResult.Type.ENTITY && target != null) {
            // TODO - chance to not cancel
            return true;
        }
        nextAttackTicks = randomAttackCooldown();
        return false;
    }

    public boolean cancelBreaking() {
        return target != null;
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
        return mc.world.getOtherEntities(camera, box)
                .stream()
                .filter(e -> e instanceof LivingEntity)
                .filter(e -> RotationUtils.angleXZ(cameraPos,rotation,e) < 1)  // ~60 degress
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
        target = findTarget();
        updateAttackTicks();
        maybeAttack();
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

    private void maybeAttack() {
        Entity targetedEntity = MinecraftClient.getInstance().targetedEntity;
        if (targetedEntity != null && nextAttackTicks == 0 && !Helper.shouldWaitForCrit()) {
            if (targetedEntity instanceof LivingEntity livingEntity) {
                if (livingEntity.hurtTime > 0 || !livingEntity.isAlive()) {
                    return;
                }
            }
            if (MinecraftClient.getInstance().options.attackKey.isPressed()) {
                ((IMinecraftClient)MinecraftClient.getInstance()).doAttackA();
                nextAttackTicks = randomAttackCooldown();
            }
        }
    }

    public void onRender(float tickDelta) {
        if (target == null) {
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!mc.options.attackKey.isPressed()) {
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

        double hSpeed = TIN.config.getHorizontalSpeed();
        double vSpeed = TIN.config.getVerticalSpeed();
        hSpeed = Helper.randomDouble(hSpeed, hSpeed*0.2, 1, 6);
        vSpeed = Helper.randomDouble(vSpeed, vSpeed*0.2, 1, 6);

        // TODO add minimum absolute value
        yawDelta = MathHelper.clamp(yawDelta, -hSpeed, hSpeed);
        pitchDelta = MathHelper.clamp(pitchDelta, -vSpeed, vSpeed);

        double cursorDeltaX = RotationUtils.angleToCursorDelta(yawDelta);
        double cursorDeltaY = RotationUtils.angleToCursorDelta(pitchDelta);
        cameraEntity.changeLookDirection(cursorDeltaX, cursorDeltaY);
    }
}
