package dev.gegy.magic.client.glyph.transform;

import dev.gegy.magic.math.AnimationTimer;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public final class BlendingGlyphTransform implements GlyphTransform {
    private final GlyphTransform source;
    private final GlyphTransform target;
    private final AnimationTimer timer;

    private final Vector3f direction = new Vector3f();

    public BlendingGlyphTransform(GlyphTransform source, GlyphTransform target, AnimationTimer timer) {
        this.source = source;
        this.target = target;
        this.timer = timer;
    }

    @Override
    public Vector3f getDirection(float tickDelta) {
        float blendProgress = this.timer.getProgress(tickDelta);

        if (blendProgress <= 0.0F) return this.source.getDirection(tickDelta);
        else if (blendProgress >= 1.0F) return this.target.getDirection(tickDelta);

        return this.source.getDirection(tickDelta).lerp(this.target.getDirection(tickDelta), blendProgress, this.direction);
    }

    @Override
    public float getDistance(final float tickDelta) {
        float blendProgress = this.timer.getProgress(tickDelta);

        if (blendProgress <= 0.0F) return this.source.getDistance(tickDelta);
        else if (blendProgress >= 1.0F) return this.target.getDistance(tickDelta);

        return Mth.lerp(blendProgress, this.source.getDistance(tickDelta), this.target.getDistance(tickDelta));
    }
}
