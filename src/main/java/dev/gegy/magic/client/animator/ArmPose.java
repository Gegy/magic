package dev.gegy.magic.client.animator;

import dev.gegy.magic.client.glyph.GlyphPlane;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public final class ArmPose {
    private static final double HALF_PI = Math.PI / 2.0;

    final Vec3f target = new Vec3f();
    final Vec3f prevTarget = new Vec3f();

    public void resetInterpolation() {
        this.prevTarget.set(this.target);
    }

    public void pointTo(LivingEntity entity, Vec3f newTarget) {
        this.prevTarget.set(this.target);

        Vec3f target = this.target;
        target.set(newTarget);

        rotateVectorRelativeToBody(target, entity);
        target.add(0.0F, entity.getStandingEyeHeight(), 0.0F);

        target.set(
                target.getX() * 16.0F,
                24.0F - target.getY() * 16.0F,
                target.getZ() * 16.0F
        );
    }

    public static void rotateVectorRelativeToBody(Vec3f vector, LivingEntity entity) {
        rotateVectorY(vector, (float) -Math.toRadians(entity.bodyYaw));
    }

    private static void rotateVectorY(Vec3f vector, float rotationY) {
        float x = vector.getX();
        float y = vector.getY();
        float z = vector.getZ();
        vector.set(
                x * MathHelper.cos(rotationY) - z * MathHelper.sin(rotationY),
                y,
                x * MathHelper.sin(rotationY) + z * MathHelper.cos(rotationY)
        );
    }

    public void pointToPointOnPlane(LivingEntity entity, GlyphPlane plane, Vec3f target) {
        plane.projectFromPlane(target);
        this.pointTo(entity, target);
    }

    public void apply(ModelPart part, float tickDelta, float weight) {
        Vec3f prevTarget = this.prevTarget;
        Vec3f target = this.target;

        float targetX = MathHelper.lerp(tickDelta, prevTarget.getX(), target.getX());
        float targetY = MathHelper.lerp(tickDelta, prevTarget.getY(), target.getY());
        float targetZ = MathHelper.lerp(tickDelta, prevTarget.getZ(), target.getZ());

        float deltaX = targetX - part.pivotX;
        float deltaY = targetY - part.pivotY;
        float deltaZ = targetZ - part.pivotZ;
        double deltaXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float targetYaw = (float) -Math.atan2(deltaX, deltaZ);
        float targetPitch = (float) (Math.atan2(deltaY, deltaXZ) - HALF_PI);

        float invWeight = 1.0F - weight;
        part.yaw = weight * targetYaw + part.yaw * invWeight;
        part.pitch = weight * targetPitch + part.pitch * invWeight;
    }
}
