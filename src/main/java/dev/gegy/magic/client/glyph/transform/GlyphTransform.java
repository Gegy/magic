package dev.gegy.magic.client.glyph.transform;

import org.joml.Vector3f;

public interface GlyphTransform {
    float DRAW_DISTANCE = 1.5f;

    static GlyphTransform of(final Vector3f direction, final float distance) {
        return new GlyphTransform() {
            @Override
            public Vector3f getDirection(final float tickDelta) {
                return direction;
            }

            @Override
            public float getDistance(final float tickDelta) {
                return distance;
            }
        };
    }

    Vector3f getDirection(float tickDelta);

    float getDistance(float tickDelta);

    default Vector3f getOrigin(final float tickDelta) {
        return new Vector3f(getDirection(tickDelta)).mul(getDistance(tickDelta));
    }
}
