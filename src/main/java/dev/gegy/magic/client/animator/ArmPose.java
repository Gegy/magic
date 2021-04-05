package dev.gegy.magic.client.animator;

import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public final class ArmPose {
    private static final double HALF_PI = Math.PI / 2.0;

    final Vec3f target = new Vec3f();
    final Vec3f prevTarget = new Vec3f();

    public void resetPrevTarget() {
        Vec3f target = this.target;
        this.prevTarget.set(target.getX(), target.getY(), target.getZ());
    }

    public void pointTo(Vec3f newTarget) {
        Vec3f target = this.target;
        this.resetPrevTarget();

        target.set(
                newTarget.getX() * 16.0F,
                24.0F - newTarget.getY() * 16.0F,
                newTarget.getZ() * 16.0F
        );
    }

    public void pointToPointOnPlane(LivingEntity entity, GlyphTransform transform, Vec3f target) {
        transform.projectFromPlane(target, 1.0F);
        SpellcastingAnimator.rotateVectorRelativeToBody(target, entity);
        target.add(0.0F, entity.getStandingEyeHeight(), 0.0F);

        this.pointTo(target);
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
