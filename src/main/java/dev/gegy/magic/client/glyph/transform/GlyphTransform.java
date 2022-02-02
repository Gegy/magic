package dev.gegy.magic.client.glyph.transform;

import net.minecraft.util.math.Vec3f;

public interface GlyphTransform {
    float DRAW_DISTANCE = 1.5F;

    static GlyphTransform of(Vec3f direction, float distance) {
        var origin = direction.copy();
        origin.scale(distance);
        return GlyphTransform.of(origin, direction);
    }

    static GlyphTransform of(Vec3f origin, Vec3f direction) {
        return new GlyphTransform() {
            @Override
            public Vec3f getOrigin(float tickDelta) {
                return origin;
            }

            @Override
            public Vec3f getDirection(float tickDelta) {
                return direction;
            }
        };
    }

    Vec3f getOrigin(float tickDelta);

    Vec3f getDirection(float tickDelta);
}
