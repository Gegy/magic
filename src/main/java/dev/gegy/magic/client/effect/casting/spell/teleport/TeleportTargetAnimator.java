package dev.gegy.magic.client.effect.casting.spell.teleport;

import com.mojang.math.Constants;
import dev.gegy.magic.math.Easings;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public final class TeleportTargetAnimator {
    private static final float ANIMATE_IN_DELAY = 3.0F;
    private static final float ANIMATE_IN_LENGTH = 8.0F;

    private static final float SPIN_SPEED = 0.025F;

    private final float innerRadius;

    private final Ring[] ringsByIndex;

    private final float animateInEnd;

    public TeleportTargetAnimator(float innerRadius, int targetCount) {
        this.innerRadius = innerRadius;

        this.ringsByIndex = this.computeRings(targetCount);

        this.animateInEnd = targetCount * ANIMATE_IN_DELAY + ANIMATE_IN_LENGTH;
    }

    private Ring[] computeRings(int totalCount) {
        var rings = new Ring[totalCount];

        int ringIndex = 0;
        int index = 0;

        while (index < totalCount) {
            float radius = this.computeRingRadius(ringIndex);
            float spin = this.computeRingDirection(ringIndex);
            int count = this.computeMaxCountInRing(radius);

            int start = index;
            int end = Math.min(index + count, totalCount);

            var ring = new Ring(start, radius, spin, end - start);
            for (int i = start; i < end; i++) {
                rings[i] = ring;
            }

            index = end;
            ringIndex++;
        }

        return rings;
    }

    public Vec2 getPosition(int index, float time) {
        var position = this.getTargetPosition(index, time);

        float animateIn = this.getAnimateInProgress(index, time);
        if (animateIn < 1.0F) {
            position = position.scale(animateIn);
        }

        return position;
    }

    private Vec2 getTargetPosition(int index, float time) {
        var ring = this.ringsByIndex[index];

        float angle = ring.getAngleFor(index, time);
        float radius = ring.radius();

        return new Vec2(
                Mth.cos(angle) * radius,
                Mth.sin(angle) * radius
        );
    }

    public float getOpacity(int index, float time) {
        return this.getAnimateInProgress(index, time);
    }

    private float getAnimateInProgress(int index, float time) {
        if (time >= this.animateInEnd) {
            return 1.0F;
        }

        float start = index * ANIMATE_IN_DELAY;
        if (time <= start) {
            return 0.0F;
        } else if (time - start >= ANIMATE_IN_LENGTH) {
            return 1.0F;
        }

        float progress = (time - start) / ANIMATE_IN_LENGTH;
        return Easings.easeOutCirc(progress);
    }

    private float computeRingRadius(int index) {
        return this.innerRadius + index * TeleportEffect.SYMBOL_SIZE;
    }

    private float computeRingDirection(int index) {
        return (index & 1) == 0 ? 1.0F : -1.0F;
    }

    private int computeMaxCountInRing(float radius) {
        float circumference = 2 * Constants.PI * radius;
        return Mth.floor(circumference / TeleportEffect.SYMBOL_SIZE);
    }

    private static final record Ring(
            int startIndex,
            float radius,
            float direction,
            int count
    ) {
        public float getAngleFor(int index, float time) {
            int localIndex = this.localIndex(index);
            float angle = ((float) localIndex / this.count) * (2.0F * Constants.PI);
            angle += time * SPIN_SPEED;
            return angle * this.direction;
        }

        private int localIndex(int index) {
            return index - this.startIndex;
        }
    }
}
