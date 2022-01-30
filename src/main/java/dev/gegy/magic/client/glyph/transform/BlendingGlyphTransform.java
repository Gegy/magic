package dev.gegy.magic.client.glyph.transform;

import dev.gegy.magic.math.AnimationTimer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public final class BlendingGlyphTransform implements GlyphTransform {
    private final GlyphTransform source;
    private final GlyphTransform target;
    private final AnimationTimer timer;

    private final Vec3f direction = new Vec3f();

    public BlendingGlyphTransform(GlyphTransform source, GlyphTransform target, AnimationTimer timer) {
        this.source = source;
        this.target = target;
        this.timer = timer;
    }

    @Override
    public float getDistance(float tickDelta) {
        float blendProgress = this.timer.getProgress(tickDelta);
        if (blendProgress <= 0.0F) {
            return this.source.getDistance(tickDelta);
        } else if (blendProgress >= 1.0F) {
            return this.target.getDistance(tickDelta);
        }
        return MathHelper.lerp(blendProgress, this.source.getDistance(tickDelta), this.target.getDistance(tickDelta));
    }

    @Override
    public Vec3f getDirection(float tickDelta) {
        float blendProgress = this.timer.getProgress(tickDelta);
        if (blendProgress <= 0.0F) {
            return this.source.getDirection(tickDelta);
        } else if (blendProgress >= 1.0F) {
            return this.target.getDirection(tickDelta);
        }

        var sourceDirection = this.source.getDirection(tickDelta);
        var targetDirection = this.target.getDirection(tickDelta);

        var direction = this.direction;
        direction.set(sourceDirection);
        direction.lerp(targetDirection, blendProgress);

        return direction;
    }
}
