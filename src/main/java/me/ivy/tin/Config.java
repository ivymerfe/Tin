package me.ivy.tin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class Config {
    public boolean DEBUG = false;

    public boolean aimbotEnabled = true;
    private int aimbotButton = 0;
    public boolean autoAttack = true;
    public boolean waitForCrits = true;
    private double missChance = 0.02;
    private double aimbotAngle = 45;
    private double horizontalSpeed = 1.5;
    private double verticalSpeed = 1;
    private int smoothTicks = 10;
    private int blockBreakingDelay = 10;
    private double noDragBase = 0.5;
    private double noDragMult = 1.6;

    public boolean showHitboxes = true;
    private double horizontalHitbox = 0.3;
    private double verticalHitbox = 0;

    public boolean showInvisible = true;
    public boolean scaleLabels = true;
    public boolean fullbright = true;
    public boolean playerESP = true;
    public boolean entityXray = true;
    public boolean noFog = true;
    public boolean noOverlays = true;
    public boolean noNausea = true;
    public double fireOffset = -0.3;
    public double heldItemOffsetX = 0.0;
    public double heldItemOffsetY = -0.1;
    public double heldItemOffsetZ = -0.2;
    public double shieldOffset = -0.2;

    public boolean isAimbotButtonPressed() {
        aimbotButton = MathHelper.clamp(aimbotButton, 0, 8);
        return GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), aimbotButton) == 1;
    }

    public double getAimbotAngle() {
        aimbotAngle = MathHelper.clamp(aimbotAngle, 10, 90);
        return aimbotAngle/180*Math.PI;
    }

    public double getMissChance() {
        missChance = MathHelper.clamp(missChance, 0, 1);
        return missChance;
    }

    public double getHorizontalSpeed() {
        horizontalSpeed = MathHelper.clamp(horizontalSpeed, 0.5, 8);
        return horizontalSpeed;
    }

    public double getVerticalSpeed() {
        verticalSpeed = MathHelper.clamp(verticalSpeed, 0.5, 8);
        return verticalSpeed;
    }

    public double getHorizontalHitbox() {
        horizontalHitbox = MathHelper.clamp(horizontalHitbox, 0, 10);
        return horizontalHitbox;
    }

    public double getVerticalHitbox() {
        verticalHitbox = MathHelper.clamp(verticalHitbox, 0, 10);
        return verticalHitbox;
    }

    public int getSmoothTicks() {
        smoothTicks = MathHelper.clamp(smoothTicks, 15, 100);
        return smoothTicks;
    }

    public int getBlockBreakingDelay() {
        blockBreakingDelay = MathHelper.clamp(blockBreakingDelay, 1, 40);
        return blockBreakingDelay;
    }

    public double getNoDragBase() {
        noDragBase = MathHelper.clamp(noDragBase, 0.2, 1);
        return noDragBase;
    }

    public double getNoDragMult() {
        noDragMult = MathHelper.clamp(noDragMult, 1.1, 4);
        return noDragMult;
    }
}
