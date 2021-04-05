package dev.gegy.magic.client.glyph.transform;

import dev.gegy.magic.math.Matrix3fAccess;
import dev.gegy.magic.math.Matrix4fAccess;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

public final class GlyphPlane implements GlyphTransform {
    public static final float DRAW_DISTANCE = 1.5F;

    private final Vec3f direction;
    private final Matrix3f glyphToWorld;
    private final Matrix3f worldToGlyph;
    private final float distance;

    private final Matrix4f renderGlyphToWorldMatrix;

    private GlyphPlane(Vec3f direction, Matrix3f glyphToWorld, Matrix3f worldToGlyph, float distance) {
        this.direction = direction;
        this.glyphToWorld = glyphToWorld;
        this.worldToGlyph = worldToGlyph;
        this.distance = distance;

        this.renderGlyphToWorldMatrix = Matrix4fAccess.create(glyphToWorld);
        this.renderGlyphToWorldMatrix.multiply(Matrix4f.scale(1.0F, 1.0F, distance));
    }

    public static GlyphPlane create(Vec3f direction, float distance) {
        Vec3f left = Vec3f.POSITIVE_Y.copy();
        left.cross(direction);
        left.normalize();

        Vec3f up = direction.copy();
        up.cross(left);
        up.normalize();

        Matrix3f glyphToWorld = Matrix3fAccess.create(
                left.getX(), up.getX(), direction.getX(),
                left.getY(), up.getY(), direction.getY(),
                left.getZ(), up.getZ(), direction.getZ()
        );

        Matrix3f worldToGlyph = glyphToWorld.copy();
        worldToGlyph.invert();

        return new GlyphPlane(direction, glyphToWorld, worldToGlyph, distance);
    }

    public GlyphPlane centered(float x, float y) {
        Vec3f direction = new Vec3f(x, y, 1.0F);
        direction.transform(this.glyphToWorld);
        return GlyphPlane.create(direction, this.distance);
    }

    @Override
    public void projectOntoPlane(Vec3f vector, float tickDelta) {
        vector.transform(this.worldToGlyph);

        // once we're in plane space, move it onto the plane by scaling such that z=distance
        vector.scale(this.distance / vector.getZ());
    }

    @Override
    public void projectFromPlane(Vec3f vector, float tickDelta) {
        vector.transform(this.glyphToWorld);
    }

    @Override
    public Matrix4f getTransformationMatrix(float tickDelta) {
        return this.renderGlyphToWorldMatrix;
    }

    @Override
    public Vec3f getDirection(float tickDelta) {
        return this.direction;
    }

    @Override
    public float getDistance(float tickDelta) {
        return this.distance;
    }

    public Vec3f getDirection() {
        return this.direction;
    }

    public float getDistance() {
        return this.distance;
    }
}
