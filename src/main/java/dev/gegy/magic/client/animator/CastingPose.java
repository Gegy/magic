package dev.gegy.magic.client.animator;

import dev.gegy.magic.client.casting.ClientCastingTracker;
import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.client.effect.casting.drawing.DrawingEffect;
import dev.gegy.magic.client.effect.casting.spell.PreparedSpellEffect;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransform;
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
            leftArm.resetInterpolation();
            rightArm.resetInterpolation();
        }

        @Override
        public boolean tick(final Player entity) {
            final EffectSelector effects = ClientCastingTracker.INSTANCE.effectSelectorFor(entity);

            final DrawingEffect drawing = effects.selectAny(DrawingEffect.TYPE);
            if (drawing == null || drawing.getGlyph() == null) {
                return false;
            }

            final ClientDrawingGlyph glyph = drawing.getGlyph();
            final Vector3f pointer = glyph.drawPointer();
            if (pointer == null) {
                return false;
            }

            final float leftX = Math.abs(pointer.x());
            final float rightX = -leftX;

            final Vector3f target = this.target.set(leftX, pointer.y(), pointer.z());
            leftArm.pointToPointOnPlane(entity, glyph.plane(), target);

            target.set(rightX, pointer.y(), pointer.z());
            rightArm.pointToPointOnPlane(entity, glyph.plane(), target);

            return true;
        }

        @Override
        public void apply(final Player entity, final ModelPart leftArm, final ModelPart rightArm, final float tickDelta, final float weight) {
            this.leftArm.apply(leftArm, tickDelta, weight);
            this.rightArm.apply(rightArm, tickDelta, weight);
        }
    }

    final class Prepared implements CastingPose {
        private final ArmPose mainArm = new ArmPose();

        @Override
        public void beginAnimating() {
            mainArm.resetInterpolation();
        }

        @Override
        public boolean tick(final Player entity) {
            final EffectSelector effects = ClientCastingTracker.INSTANCE.effectSelectorFor(entity);
            final PreparedSpellEffect preparedSpell = effects.selectAny(PreparedSpellEffect.TYPE);
            if (preparedSpell == null) {
                return false;
            }

            final SpellTransform transform = preparedSpell.spell().transform();

            final Vector3f target = transform.getOrigin(1.0f);
            mainArm.pointTo(entity, target);

            return true;
        }

        @Override
        public void apply(final Player entity, final ModelPart leftArm, final ModelPart rightArm, final float tickDelta, final float weight) {
            final ModelPart armPart = entity.getMainArm() == HumanoidArm.LEFT ? leftArm : rightArm;
            mainArm.apply(armPart, tickDelta, weight);
        }
    }
}
