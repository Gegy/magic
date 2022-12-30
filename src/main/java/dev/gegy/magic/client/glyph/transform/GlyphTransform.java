package dev.gegy.magic.client.glyph.transform;

import org.joml.Vector3f;

public interface GlyphTransform {
    float DRAW_DISTANCE = 1.5F;

    static GlyphTransform of(Vector3f direction, float distance) {
        return new GlyphTransform() {
            @Override
            public Vector3f getDirection(float tickDelta) {
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

    default Vector3f getOrigin(float tickDelta) {
        return new Vector3f(this.getDirection(tickDelta)).mul(this.getDistance(tickDelta));
    }
}
