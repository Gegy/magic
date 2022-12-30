package dev.gegy.magic.client.glyph.transform;

import net.minecraft.util.math.Vec3f;

public interface GlyphTransform {
    float DRAW_DISTANCE = 1.5F;

    static GlyphTransform of(Vec3f direction, float distance) {
        return new GlyphTransform() {
            @Override
            public Vec3f getDirection(float tickDelta) {
                return direction;
            }

            @Override
            public float getDistance(final float tickDelta) {
                return distance;
            }
        };
    }

    default Vec3f getOrigin(float tickDelta) {
        final Vec3f origin = this.getDirection(tickDelta).copy();
        origin.scale(this.getDistance(tickDelta));
        return origin;
    }

    Vec3f getDirection(float tickDelta);

    float getDistance(float tickDelta);
}
