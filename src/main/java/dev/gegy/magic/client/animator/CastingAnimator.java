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

    public void applyToModel(Player entity, ModelPart leftArm, ModelPart rightArm, float tickDelta) {
        CastingPose pose = this.pose;
        CastingPose prevPose = this.prevPose;
        if (pose == null && prevPose == null) {
            return;
        }

        if (pose == prevPose) {
            this.applyToModelStable(entity, pose, leftArm, rightArm, tickDelta);
        } else {
            float blendTicks = this.poseBlendingTicks + tickDelta;
            this.applyToModelBlended(entity, prevPose, pose, blendTicks, leftArm, rightArm, tickDelta);
        }
    }

    private void applyToModelStable(Player entity, CastingPose pose, ModelPart leftArm, ModelPart rightArm, float tickDelta) {
        pose.apply(entity, leftArm, rightArm, tickDelta, 1.0F);
    }

    private void applyToModelBlended(
            Player entity,
            CastingPose prevPose, CastingPose pose, float transitionTicks,
            ModelPart leftArm, ModelPart rightArm, float tickDelta
    ) {
        float weight = transitionTicks / POSE_BLEND_TICKS;
        float prevWeight = 1.0F - weight;

        if (prevPose != null) {
            prevPose.apply(entity, leftArm, rightArm, tickDelta, prevWeight);
        }

        if (pose != null) {
            pose.apply(entity, leftArm, rightArm, tickDelta, weight);
        }
    }

    public void tick(Player entity) {
        if (this.pose == this.prevPose) {
            this.tickStable(entity);
        } else {
            this.tickBlending(entity);
        }
    }

    private void tickStable(Player entity) {
        if (this.tickPose(entity, this.drawing)) return;
        if (this.tickPose(entity, this.prepared)) return;

        this.pose = null;
    }

    private boolean tickPose(Player entity, CastingPose pose) {
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

    private void tickBlending(Player entity) {
        if (++this.poseBlendingTicks >= POSE_BLEND_TICKS) {
            this.poseBlendingTicks = 0;
            this.prevPose = this.pose;
        }

        // just tick our current pose: we don't want to change poses while blending
        if (this.pose != null) {
            this.pose.tick(entity);
        }
    }
}
