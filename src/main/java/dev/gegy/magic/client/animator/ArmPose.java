package dev.gegy.magic.client.animator;

import dev.gegy.magic.client.glyph.GlyphPlane;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

public final class ArmPose {
    private static final double HALF_PI = Math.PI / 2.0;

    private final Vector3f target = new Vector3f();
    private final Vector3f prevTarget = new Vector3f();

    public void resetInterpolation() {
        this.prevTarget.set(this.target);
    }

    public void pointTo(LivingEntity entity, Vector3f newTarget) {
        this.prevTarget.set(this.target);

        var target = this.target.set(newTarget)
                .rotateY(entity.bodyYaw * MathHelper.RADIANS_PER_DEGREE)
                .add(0.0F, entity.getStandingEyeHeight(), 0.0F);

        target.set(
                target.x() * 16.0F,
                24.0F - target.y() * 16.0F,
                target.z() * 16.0F
        );
    }

    public void pointToPointOnPlane(LivingEntity entity, GlyphPlane plane, Vector3f target) {
        this.pointTo(entity, plane.projectToWorld(target));
    }

    public void apply(ModelPart part, float tickDelta, float weight) {
        Vector3f prevTarget = this.prevTarget;
        Vector3f target = this.target;

        float targetX = MathHelper.lerp(tickDelta, prevTarget.x(), target.x());
        float targetY = MathHelper.lerp(tickDelta, prevTarget.y(), target.y());
        float targetZ = MathHelper.lerp(tickDelta, prevTarget.z(), target.z());

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
