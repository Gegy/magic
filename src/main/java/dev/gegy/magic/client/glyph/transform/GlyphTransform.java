package dev.gegy.magic.client.glyph.transform;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public interface GlyphTransform {
    default void tick() {
    }

    Vec3f getDirection(float tickDelta);

    float getDistance(float tickDelta);

    Matrix4f getTransformationMatrix(float tickDelta);

    void projectOntoPlane(Vec3f vector, float tickDelta);

    default void projectOntoPlane(Vec3f vector) {
        this.projectOntoPlane(vector, 1.0F);
    }

    void projectFromPlane(Vec3f vector, float tickDelta);

    default void projectFromPlane(Vec3f vector) {
        this.projectFromPlane(vector, 1.0F);
    }

    default Vec3f projectFromPlane(float x, float y, float z) {
        Vec3f vector = new Vec3f(x, y, z);
        this.projectFromPlane(vector);
        return vector;
    }
}
