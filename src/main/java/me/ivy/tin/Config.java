package me.ivy.tin;

import net.minecraft.util.math.MathHelper;

public class Config {
    public boolean aimbotEnabled = true;
    public boolean autoAttack = true;
    private double horizontalSpeed = 3;
    private double verticalSpeed = 2;

    public boolean showHitboxes = true;
    private double horizontalHitbox = 0.3;
    private double verticalHitbox = 0;

    public boolean showInvisible = true;
    public boolean scaleLabels = true;
    public boolean fullbright = true;
    public boolean playerESP = true;
    public boolean noBlindness = true;
    public boolean noNausea = true;
    public boolean noOverlays = true;
    public double fireOffset = 16;
    public double shieldOffset = 6;

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
}
