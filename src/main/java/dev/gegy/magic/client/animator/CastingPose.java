package dev.gegy.magic.client.animator;

import dev.gegy.magic.client.casting.ClientCastingTracker;
import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.effect.casting.drawing.DrawingEffect;
import dev.gegy.magic.client.effect.casting.spell.PreparedSpellEffect;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

public interface CastingPose {
    void beginAnimating();

    boolean tick(Player entity);

    void apply(Player entity, ModelPart leftArm, ModelPart rightArm, float tickDelta, float weight);

    final class Drawing implements CastingPose {
        private final ArmPose leftArm = new ArmPose();
        private final ArmPose rightArm = new ArmPose();

        private final Vector3f target = new Vector3f();

        @Override
        public void beginAnimating() {
            this.leftArm.resetInterpolation();
            this.rightArm.resetInterpolation();
        }

        @Override
        public boolean tick(Player entity) {
            var effects = ClientCastingTracker.INSTANCE.effectSelectorFor(entity);

            DrawingEffect drawing = effects.selectAny(DrawingEffect.TYPE);
            if (drawing == null || drawing.getGlyph() == null) {
                return false;
            }

            ClientDrawingGlyph glyph = drawing.getGlyph();
            Vector3f pointer = glyph.drawPointer();
            if (pointer == null) {
                return false;
            }

            float leftX = Math.abs(pointer.x());
            float rightX = -leftX;

            Vector3f target = this.target.set(leftX, pointer.y(), pointer.z());
            this.leftArm.pointToPointOnPlane(entity, glyph.plane(), target);

            target.set(rightX, pointer.y(), pointer.z());
            this.rightArm.pointToPointOnPlane(entity, glyph.plane(), target);

            return true;
        }

        @Override
        public void apply(Player entity, ModelPart leftArm, ModelPart rightArm, float tickDelta, float weight) {
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
        public boolean tick(Player entity) {
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
        public void apply(Player entity, ModelPart leftArm, ModelPart rightArm, float tickDelta, float weight) {
            ModelPart armPart = entity.getMainArm() == HumanoidArm.LEFT ? leftArm : rightArm;
            this.mainArm.apply(armPart, tickDelta, weight);
        }
    }
}
