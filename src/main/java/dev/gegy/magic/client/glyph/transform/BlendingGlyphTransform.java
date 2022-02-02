package dev.gegy.magic.client.glyph.transform;

import dev.gegy.magic.math.AnimationTimer;
import net.minecraft.util.math.Vec3f;

public final class BlendingGlyphTransform implements GlyphTransform {
    private final GlyphTransform source;
    private final GlyphTransform target;
    private final AnimationTimer timer;

    private final Vec3f origin = new Vec3f();
    private final Vec3f direction = new Vec3f();

    public BlendingGlyphTransform(GlyphTransform source, GlyphTransform target, AnimationTimer timer) {
        this.source = source;
        this.target = target;
        this.timer = timer;
    }

    // TODO: we might want to blend in polar coordinates
    @Override
    public Vec3f getOrigin(float tickDelta) {
        float blendProgress = this.timer.getProgress(tickDelta);

        if (blendProgress <= 0.0F) return this.source.getOrigin(tickDelta);
        else if (blendProgress >= 1.0F) return this.target.getOrigin(tickDelta);

        return this.blend(this.origin, blendProgress,
                this.source.getOrigin(tickDelta),
                this.target.getOrigin(tickDelta)
        );
    }

    @Override
    public Vec3f getDirection(float tickDelta) {
        float blendProgress = this.timer.getProgress(tickDelta);

        if (blendProgress <= 0.0F) return this.source.getDirection(tickDelta);
        else if (blendProgress >= 1.0F) return this.target.getDirection(tickDelta);

        return this.blend(this.direction, blendProgress,
                this.source.getDirection(tickDelta),
                this.target.getDirection(tickDelta)
        );
    }

    private Vec3f blend(Vec3f result, float progress, Vec3f source, Vec3f target) {
        result.set(source);
        result.lerp(target, progress);
        return result;
    }
}
