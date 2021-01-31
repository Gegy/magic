package dev.gegy.magic.client.animator;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public final class SpellcastingAnimator {
    private static final int POSE_TRANSITION_TICKS = 8;

    private final SpellcastingPose prepared = new SpellcastingPose.Prepared();
    private final SpellcastingPose drawing = new SpellcastingPose.Drawing();

    private SpellcastingPose pose;
    private SpellcastingPose prevPose;
    private int poseTransitionTicks;

    public void applyToModel(LivingEntity entity, ModelPart leftArm, ModelPart rightArm, float tickDelta) {
        SpellcastingPose pose = this.pose;
        SpellcastingPose prevPose = this.prevPose;
        if (pose == null && prevPose == null) {
            return;
        }

        if (pose == prevPose) {
            this.applyToModelStable(entity, pose, leftArm, rightArm, tickDelta);
        } else {
            float transitionTicks = this.poseTransitionTicks + tickDelta;
            this.applyToModelTransitioning(entity, prevPose, pose, transitionTicks, leftArm, rightArm, tickDelta);
        }
    }

    private void applyToModelStable(LivingEntity entity, SpellcastingPose pose, ModelPart leftArm, ModelPart rightArm, float tickDelta) {
        pose.apply(entity, leftArm, rightArm, tickDelta, 1.0F);
    }

    private void applyToModelTransitioning(
            LivingEntity entity,
            SpellcastingPose prevPose, SpellcastingPose pose, float transitionTicks,
            ModelPart leftArm, ModelPart rightArm, float tickDelta
    ) {
        float weight = transitionTicks / POSE_TRANSITION_TICKS;
        float prevWeight = 1.0F - weight;

        if (prevPose != null) {
            prevPose.apply(entity, leftArm, rightArm, tickDelta, prevWeight);
        }

        if (pose != null) {
            pose.apply(entity, leftArm, rightArm, tickDelta, weight);
        }
    }

    public void tick(LivingEntity entity) {
        if (this.pose == this.prevPose) {
            this.tickStable(entity);
        } else {
            this.tickTransitioning(entity);
        }
    }

    private void tickStable(LivingEntity entity) {
        if (this.tickPose(entity, this.drawing)) return;
        if (this.tickPose(entity, this.prepared)) return;

        this.pose = null;
    }

    private boolean tickPose(LivingEntity entity, SpellcastingPose pose) {
        if (pose.tick(entity)) {
            if (this.pose != pose) {
                this.pose = pose;
                pose.beginAnimating();
            }
            return true;
        } else {
            return false;
        }
    }

    private void tickTransitioning(LivingEntity entity) {
        if (++this.poseTransitionTicks >= POSE_TRANSITION_TICKS) {
            this.poseTransitionTicks = 0;
            this.prevPose = this.pose;
        }

        // just tick our current pose: we don't want to change poses while animating
        if (this.pose != null) {
            this.pose.tick(entity);
        }
    }

    public static void rotateVectorRelativeToBody(Vector3f vector, LivingEntity entity) {
        rotateVectorY(vector, (float) -Math.toRadians(entity.bodyYaw));
    }

    private static void rotateVectorY(Vector3f vector, float rotationY) {
        float x = vector.getX();
        float y = vector.getY();
        float z = vector.getZ();
        vector.set(
                x * MathHelper.cos(rotationY) - z * MathHelper.sin(rotationY),
                y,
                x * MathHelper.sin(rotationY) + z * MathHelper.cos(rotationY)
        );
    }
}
