package dev.gegy.magic.client.glyph.transform;

import dev.gegy.magic.math.AnimationTimer;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public final class BlendingGlyphTransform implements GlyphTransform {
    private final GlyphTransform source;
    private final GlyphTransform target;
    private final AnimationTimer timer;

    private final Vector3f direction = new Vector3f();

    public BlendingGlyphTransform(final GlyphTransform source, final GlyphTransform target, final AnimationTimer timer) {
        this.source = source;
        this.target = target;
        this.timer = timer;
    }

    @Override
    public Vector3f getDirection(final float tickDelta) {
        final float blendProgress = timer.getProgress(tickDelta);

        if (blendProgress <= 0.0f) {
            return source.getDirection(tickDelta);
        } else if (blendProgress >= 1.0f) {
            return target.getDirection(tickDelta);
        }

        return source.getDirection(tickDelta).lerp(target.getDirection(tickDelta), blendProgress, direction);
    }

    @Override
    public float getDistance(final float tickDelta) {
        final float blendProgress = timer.getProgress(tickDelta);

        if (blendProgress <= 0.0f) {
            return source.getDistance(tickDelta);
        } else if (blendProgress >= 1.0f) {
            return target.getDistance(tickDelta);
        }

        return Mth.lerp(blendProgress, source.getDistance(tickDelta), target.getDistance(tickDelta));
    }
}
