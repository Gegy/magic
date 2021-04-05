package dev.gegy.magic.client.animator;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.ClientGlyphTracker;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3f;

import java.util.List;

public interface SpellcastingPose {
    void beginAnimating();

    boolean tick(LivingEntity entity);

    void apply(LivingEntity entity, ModelPart leftArm, ModelPart rightArm, float tickDelta, float weight);

    final class Drawing implements SpellcastingPose {
        private final ArmPose leftArm = new ArmPose();
        private final ArmPose rightArm = new ArmPose();

        private final Vec3f target = new Vec3f();

        @Override
        public void beginAnimating() {
            this.leftArm.resetPrevTarget();
            this.rightArm.resetPrevTarget();
        }

        @Override
        public boolean tick(LivingEntity entity) {
            ClientGlyph glyph = ClientGlyphTracker.INSTANCE.getDrawingGlyphFor(entity);
            if (glyph == null) {
                return false;
            }

            Vec3f point = glyph.getLookingAt();

            float leftX = Math.abs(point.getX());
            float rightX = -leftX;

            Vec3f target = this.target;
            target.set(leftX, point.getY(), point.getZ());
            this.leftArm.pointToPointOnPlane(entity, glyph.transform, target);

            target.set(rightX, point.getY(), point.getZ());
            this.rightArm.pointToPointOnPlane(entity, glyph.transform, target);

            return true;
        }

        @Override
        public void apply(LivingEntity entity, ModelPart leftArm, ModelPart rightArm, float tickDelta, float weight) {
            this.leftArm.apply(leftArm, tickDelta, weight);
            this.rightArm.apply(rightArm, tickDelta, weight);
        }
    }

    final class Prepared implements SpellcastingPose {
        private final ArmPose mainArm = new ArmPose();
        private final Vec3f target = new Vec3f();

        @Override
        public void beginAnimating() {
            this.mainArm.resetPrevTarget();
        }

        @Override
        public boolean tick(LivingEntity entity) {
            List<ClientGlyph> preparedGlyphs = ClientGlyphTracker.INSTANCE.getPreparedGlyphsFor(entity);
            if (preparedGlyphs.isEmpty()) {
                return false;
            }

            ClientGlyph glyph = preparedGlyphs.get(0);

            Vec3f direction = glyph.transform.getDirection(1.0F);
            SpellcastingAnimator.rotateVectorRelativeToBody(direction, entity);

            float distance = glyph.transform.getDistance(1.0F);

            Vec3f target = this.target;
            target.set(
                    direction.getX() * distance,
                    entity.getStandingEyeHeight() + direction.getY() * distance,
                    direction.getZ() * distance
            );

            this.mainArm.pointTo(target);

            return true;
        }

        @Override
        public void apply(LivingEntity entity, ModelPart leftArm, ModelPart rightArm, float tickDelta, float weight) {
            ModelPart armPart = entity.getMainArm() == Arm.LEFT ? leftArm : rightArm;
            this.mainArm.apply(armPart, tickDelta, weight);
        }
    }
}
