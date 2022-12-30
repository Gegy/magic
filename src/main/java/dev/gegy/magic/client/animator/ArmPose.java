package dev.gegy.magic.client.animator;

import dev.gegy.magic.client.glyph.GlyphPlane;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

public final class ArmPose {
    private static final double HALF_PI = Math.PI / 2.0;

    private final Vector3f target = new Vector3f();
    private final Vector3f prevTarget = new Vector3f();

    public void resetInterpolation() {
        prevTarget.set(target);
    }

    public void pointTo(final LivingEntity entity, final Vector3f newTarget) {
        prevTarget.set(target);

        final Vector3f target = this.target.set(newTarget)
                .rotateY(entity.yBodyRot * Mth.DEG_TO_RAD)
                .add(0.0f, entity.getEyeHeight(), 0.0f);

        target.set(
                target.x() * 16.0f,
                24.0f - target.y() * 16.0f,
                target.z() * 16.0f
        );
    }

    public void pointToPointOnPlane(final LivingEntity entity, final GlyphPlane plane, final Vector3f target) {
        pointTo(entity, plane.projectToWorld(target));
    }

    public void apply(final ModelPart part, final float tickDelta, final float weight) {
        final Vector3f prevTarget = this.prevTarget;
        final Vector3f target = this.target;

        final float targetX = Mth.lerp(tickDelta, prevTarget.x(), target.x());
        final float targetY = Mth.lerp(tickDelta, prevTarget.y(), target.y());
        final float targetZ = Mth.lerp(tickDelta, prevTarget.z(), target.z());

        final float deltaX = targetX - part.x;
        final float deltaY = targetY - part.y;
        final float deltaZ = targetZ - part.z;
        final double deltaXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        final float targetYaw = (float) -Math.atan2(deltaX, deltaZ);
        final float targetPitch = (float) (Math.atan2(deltaY, deltaXZ) - HALF_PI);

        final float invWeight = 1.0f - weight;
        part.yRot = weight * targetYaw + part.yRot * invWeight;
        part.xRot = weight * targetPitch + part.xRot * invWeight;
    }
}
