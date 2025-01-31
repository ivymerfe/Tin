package me.ivy.tin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils
{
	public static double angleXZ(Vec3d pos, Vec3d rotation, Entity target)
	{
		Vec3d direction = target.getBoundingBox().getCenter().subtract(pos);
		double dX = direction.x;
		double dZ = direction.z;
		double rX = rotation.x;
		double rZ = rotation.z;
		double d = Math.sqrt(dX*dX + dZ*dZ);
		double r = Math.sqrt(rX*rX + rZ*rZ);
		return Math.acos((dX*rX+dZ*rZ)/d/r);
	}

	public static Vec3d getLerpedPos(Entity e, float tickDelta)
	{
		// When an entity is removed, it stops moving and its lastRenderX/Y/Z
		// values are no longer updated.
		if(e.isRemoved())
			return e.getPos();

		double x = MathHelper.lerp(tickDelta, e.lastRenderX, e.getX());
		double y = MathHelper.lerp(tickDelta, e.lastRenderY, e.getY());
		double z = MathHelper.lerp(tickDelta, e.lastRenderZ, e.getZ());
		return new Vec3d(x, y, z);
	}

	public static Box getLerpedBox(Entity e, float tickDelta)
	{
		// When an entity is removed, it stops moving and its lastRenderX/Y/Z
		// values are no longer updated.
		if(e.isRemoved())
			return e.getBoundingBox();

		Vec3d offset = getLerpedPos(e, tickDelta).subtract(e.getPos());
		return e.getBoundingBox().offset(offset);
	}

	public static double getNeededYaw(Vec3d cameraPos, Vec3d target) {
		double diffX = target.x - cameraPos.x;
		double diffZ = target.z - cameraPos.z;
		return Math.toDegrees(Math.atan2(diffZ, diffX)) - 90;
	}

	public static double getNeededPitch(Vec3d cameraPos, Vec3d target) {
		double diffX = target.x - cameraPos.x;
		double diffY = target.y - cameraPos.y;
		double diffZ = target.z - cameraPos.z;
		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
		return -Math.toDegrees(Math.atan2(diffY, diffXZ));
	}

	public static double getMouseSensivityMultiplier() {
		double d = MinecraftClient.getInstance().options.getMouseSensitivity().getValue() * 0.6F + 0.2F;
		double e = d * d * d;
		double f = e * 8.0;
		return f;
	}

	public static double angleToCursorDelta(double angle) {
		double m = getMouseSensivityMultiplier();
		double deltaRestored = angle / 0.15;  // Multiplier from changeLookDirection
		return Math.round(deltaRestored/m)*m;  // Bypass detection: mouseX and mouseY = integer!
	}
}
