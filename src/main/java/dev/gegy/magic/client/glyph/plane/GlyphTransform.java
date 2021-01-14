package dev.gegy.magic.client.glyph.plane;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Matrix4f;

public interface GlyphTransform {
    default void tick() {
    }

    Vector3f getDirection(float tickDelta);

    float getDistance(float tickDelta);

    Matrix4f getTransformationMatrix(float tickDelta);

    void projectOntoPlane(Vector3f vector, float tickDelta);

    void projectFromPlane(Vector3f vector, float tickDelta);
}
