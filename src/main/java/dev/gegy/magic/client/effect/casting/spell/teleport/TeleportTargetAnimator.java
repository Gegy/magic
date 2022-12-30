package dev.gegy.magic.client.effect.casting.spell.teleport;

import com.mojang.math.Constants;
import dev.gegy.magic.math.Easings;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public final class TeleportTargetAnimator {
    private static final float ANIMATE_IN_DELAY = 3.0f;
    private static final float ANIMATE_IN_LENGTH = 8.0f;

    private static final float SPIN_SPEED = 0.025f;

    private final float innerRadius;

    private final Ring[] ringsByIndex;

    private final float animateInEnd;

    public TeleportTargetAnimator(final float innerRadius, final int targetCount) {
        this.innerRadius = innerRadius;

        ringsByIndex = computeRings(targetCount);

        animateInEnd = targetCount * ANIMATE_IN_DELAY + ANIMATE_IN_LENGTH;
    }

    private Ring[] computeRings(final int totalCount) {
        final Ring[] rings = new Ring[totalCount];

        int ringIndex = 0;
        int index = 0;

        while (index < totalCount) {
            final float radius = computeRingRadius(ringIndex);
            final float spin = computeRingDirection(ringIndex);
            final int count = computeMaxCountInRing(radius);

            final int start = index;
            final int end = Math.min(index + count, totalCount);

            final Ring ring = new Ring(start, radius, spin, end - start);
            for (int i = start; i < end; i++) {
                rings[i] = ring;
            }

            index = end;
            ringIndex++;
        }

        return rings;
    }

    public Vec2 getPosition(final int index, final float time) {
        Vec2 position = getTargetPosition(index, time);

        final float animateIn = getAnimateInProgress(index, time);
        if (animateIn < 1.0f) {
            position = position.scale(animateIn);
        }

        return position;
    }

    private Vec2 getTargetPosition(final int index, final float time) {
        final Ring ring = ringsByIndex[index];

        final float angle = ring.getAngleFor(index, time);
        final float radius = ring.radius();

        return new Vec2(
                Mth.cos(angle) * radius,
                Mth.sin(angle) * radius
        );
    }

    public float getOpacity(final int index, final float time) {
        return getAnimateInProgress(index, time);
    }

    private float getAnimateInProgress(final int index, final float time) {
        if (time >= animateInEnd) {
            return 1.0f;
        }

        final float start = index * ANIMATE_IN_DELAY;
        if (time <= start) {
            return 0.0f;
        } else if (time - start >= ANIMATE_IN_LENGTH) {
            return 1.0f;
        }

        final float progress = (time - start) / ANIMATE_IN_LENGTH;
        return Easings.easeOutCirc(progress);
    }

    private float computeRingRadius(final int index) {
        return innerRadius + index * TeleportEffect.SYMBOL_SIZE;
    }

    private float computeRingDirection(final int index) {
        return (index & 1) == 0 ? 1.0f : -1.0f;
    }

    private int computeMaxCountInRing(final float radius) {
        final float circumference = 2 * Constants.PI * radius;
        return Mth.floor(circumference / TeleportEffect.SYMBOL_SIZE);
    }

    private record Ring(
            int startIndex,
            float radius,
            float direction,
            int count
    ) {
        public float getAngleFor(final int index, final float time) {
            final int localIndex = localIndex(index);
            float angle = ((float) localIndex / count) * (2.0f * Constants.PI);
            angle += time * SPIN_SPEED;
            return angle * direction;
        }

        private int localIndex(final int index) {
            return index - startIndex;
        }
    }
}
