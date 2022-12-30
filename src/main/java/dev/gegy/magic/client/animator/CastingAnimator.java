package dev.gegy.magic.client.animator;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.player.Player;

public final class CastingAnimator {
    private static final int POSE_BLEND_TICKS = 5;

    private final CastingPose prepared = new CastingPose.Prepared();
    private final CastingPose drawing = new CastingPose.Drawing();

    private CastingPose pose;
    private CastingPose prevPose;
    private int poseBlendingTicks;

    public void applyToModel(final Player entity, final ModelPart leftArm, final ModelPart rightArm, final float tickDelta) {
        final CastingPose pose = this.pose;
        final CastingPose prevPose = this.prevPose;
        if (pose == null && prevPose == null) {
            return;
        }

        if (pose == prevPose) {
            applyToModelStable(entity, pose, leftArm, rightArm, tickDelta);
        } else {
            final float blendTicks = poseBlendingTicks + tickDelta;
            applyToModelBlended(entity, prevPose, pose, blendTicks, leftArm, rightArm, tickDelta);
        }
    }

    private void applyToModelStable(final Player entity, final CastingPose pose, final ModelPart leftArm, final ModelPart rightArm, final float tickDelta) {
        pose.apply(entity, leftArm, rightArm, tickDelta, 1.0f);
    }

    private void applyToModelBlended(
            final Player entity,
            final CastingPose prevPose, final CastingPose pose, final float transitionTicks,
            final ModelPart leftArm, final ModelPart rightArm, final float tickDelta
    ) {
        final float weight = transitionTicks / POSE_BLEND_TICKS;
        final float prevWeight = 1.0f - weight;

        if (prevPose != null) {
            prevPose.apply(entity, leftArm, rightArm, tickDelta, prevWeight);
        }

        if (pose != null) {
            pose.apply(entity, leftArm, rightArm, tickDelta, weight);
        }
    }

    public void tick(final Player entity) {
        if (pose == prevPose) {
            tickStable(entity);
        } else {
            tickBlending(entity);
        }
    }

    private void tickStable(final Player entity) {
        if (tickPose(entity, drawing)) return;
        if (tickPose(entity, prepared)) return;

        pose = null;
    }

    private boolean tickPose(final Player entity, final CastingPose pose) {
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

    private void tickBlending(final Player entity) {
        if (++poseBlendingTicks >= POSE_BLEND_TICKS) {
            poseBlendingTicks = 0;
            prevPose = pose;
        }

        // just tick our current pose: we don't want to change poses while blending
        if (pose != null) {
            pose.tick(entity);
        }
    }
}
