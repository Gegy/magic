package dev.gegy.magic.client.animator;

import dev.gegy.magic.client.casting.ClientCastingTracker;
import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.effect.casting.drawing.DrawingEffect;
import dev.gegy.magic.client.effect.casting.spell.PreparedSpellEffect;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3f;

public interface CastingPose {
    void beginAnimating();

    boolean tick(PlayerEntity entity);

    void apply(PlayerEntity entity, ModelPart leftArm, ModelPart rightArm, float tickDelta, float weight);

    final class Drawing implements CastingPose {
        private final ArmPose leftArm = new ArmPose();
        private final ArmPose rightArm = new ArmPose();

        private final Vec3f target = new Vec3f();

        @Override
        public void beginAnimating() {
            this.leftArm.resetInterpolation();
            this.rightArm.resetInterpolation();
        }

        @Override
        public boolean tick(PlayerEntity entity) {
            var effects = ClientCastingTracker.INSTANCE.effectSelectorFor(entity);

            DrawingEffect drawing = effects.selectAny(DrawingEffect.TYPE);
            if (drawing == null || drawing.getGlyph() == null) {
                return false;
            }

            ClientDrawingGlyph glyph = drawing.getGlyph();
            Vec3f pointer = glyph.drawPointer();
            if (pointer == null) {
                return false;
            }

            float leftX = Math.abs(pointer.getX());
            float rightX = -leftX;

            Vec3f target = this.target;
            target.set(leftX, pointer.getY(), pointer.getZ());
            this.leftArm.pointToPointOnPlane(entity, glyph.plane(), target);

            target.set(rightX, pointer.getY(), pointer.getZ());
            this.rightArm.pointToPointOnPlane(entity, glyph.plane(), target);

            return true;
        }

        @Override
        public void apply(PlayerEntity entity, ModelPart leftArm, ModelPart rightArm, float tickDelta, float weight) {
            this.leftArm.apply(leftArm, tickDelta, weight);
            this.rightArm.apply(rightArm, tickDelta, weight);
        }
    }

    final class Prepared implements CastingPose {
        private final ArmPose mainArm = new ArmPose();

        @Override
        public void beginAnimating() {
            this.mainArm.resetInterpolation();
        }

        @Override
        public boolean tick(PlayerEntity entity) {
            var effects = ClientCastingTracker.INSTANCE.effectSelectorFor(entity);
            var preparedSpell = effects.selectAny(PreparedSpellEffect.TYPE);
            if (preparedSpell == null) {
                return false;
            }

            var transform = preparedSpell.spell().transform();

            var target = transform.getOrigin(1.0F);
            this.mainArm.pointTo(entity, target);

            return true;
        }

        @Override
        public void apply(PlayerEntity entity, ModelPart leftArm, ModelPart rightArm, float tickDelta, float weight) {
            ModelPart armPart = entity.getMainArm() == Arm.LEFT ? leftArm : rightArm;
            this.mainArm.apply(armPart, tickDelta, weight);
        }
    }
}
