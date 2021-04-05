package dev.gegy.magic.client.glyph.plane;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public interface GlyphTransform {
    default void tick() {
    }

    Vec3f getDirection(float tickDelta);

    float getDistance(float tickDelta);

    Matrix4f getTransformationMatrix(float tickDelta);

    void projectOntoPlane(Vec3f vector, float tickDelta);

    void projectFromPlane(Vec3f vector, float tickDelta);
}
