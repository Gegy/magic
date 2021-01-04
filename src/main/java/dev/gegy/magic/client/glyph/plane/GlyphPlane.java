package dev.gegy.magic.client.glyph.plane;

import dev.gegy.magic.math.Matrix3fAccess;
import dev.gegy.magic.math.Matrix4fAccess;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

public final class GlyphPlane implements GlyphTransform {
    public static final float DRAW_DISTANCE = 1.5F;

    private final Vector3f direction;
    private final Matrix3f glyphToWorld;
    private final Matrix3f worldToGlyph;
    private final float distance;

    private final Matrix4f renderGlyphToWorldMatrix;

    private GlyphPlane(Vector3f direction, Matrix3f glyphToWorld, Matrix3f worldToGlyph, float distance) {
        this.direction = direction;
        this.glyphToWorld = glyphToWorld;
        this.worldToGlyph = worldToGlyph;
        this.distance = distance;

        this.renderGlyphToWorldMatrix = Matrix4fAccess.create(glyphToWorld);
        this.renderGlyphToWorldMatrix.multiply(Matrix4f.scale(1.0F, 1.0F, distance));
    }

    public static GlyphPlane create(Vector3f direction, float distance) {
        Vector3f left = Vector3f.POSITIVE_Y.copy();
        left.cross(direction);
        left.normalize();

        Vector3f up = direction.copy();
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
        Vector3f direction = new Vector3f(x, y, 1.0F);
        direction.transform(this.glyphToWorld);
        return GlyphPlane.create(direction, this.distance);
    }

    public void projectOntoPlane(Vector3f vector) {
        vector.transform(this.worldToGlyph);

        // once we're in plane space, move it onto the plane by scaling such that z=distance
        vector.scale(this.distance / vector.getZ());
    }

    @Override
    public Matrix4f getTransformationMatrix(float tickDelta) {
        return this.renderGlyphToWorldMatrix;
    }

    @Override
    public Vector3f getDirection(float tickDelta) {
        return this.direction;
    }

    @Override
    public float getDistance(float tickDelta) {
        return this.distance;
    }

    public Vector3f getDirection() {
        return this.direction;
    }

    public float getDistance() {
        return this.distance;
    }
}
