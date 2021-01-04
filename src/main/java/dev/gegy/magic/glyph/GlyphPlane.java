package dev.gegy.magic.glyph;

import dev.gegy.magic.math.Matrix3fAccess;
import dev.gegy.magic.math.Matrix4fAccess;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

public final class GlyphPlane {
    public static final float DRAW_DISTANCE = 1.5F;

    private final Vector3f direction = new Vector3f();
    private final Vector3f left = new Vector3f();
    private final Vector3f up = new Vector3f();

    private float distance = 1.0F;

    private final Matrix3f glyphToWorld = new Matrix3f();
    private final Matrix3f worldToGlyph = new Matrix3f();

    private final Matrix4f renderGlyphToWorldMatrix = new Matrix4f();

    private GlyphPlane() {
    }

    public static GlyphPlane create(Vector3f direction, float distance) {
        GlyphPlane plane = new GlyphPlane();
        plane.set(direction, distance);
        return plane;
    }

    public static GlyphPlane create(float directionX, float directionY, float directionZ, float distance) {
        GlyphPlane plane = new GlyphPlane();
        plane.set(directionX, directionY, directionZ, distance);
        return plane;
    }

    public void set(Vector3f direction, float distance) {
        this.set(direction.getX(), direction.getY(), direction.getZ(), distance);
    }

    public void set(float directionX, float directionY, float directionZ, float distance) {
        this.direction.set(directionX, directionY, directionZ);
        this.distance = distance;

        Vector3f left = this.left;
        Vector3f up = this.up;

        left.set(0.0F, 1.0F, 0.0F);
        left.cross(this.direction);
        left.normalize();

        up.set(directionX, directionY, directionZ);
        up.cross(left);
        up.normalize();

        Matrix3fAccess.set(this.glyphToWorld,
                left.getX(), up.getX(), directionX,
                left.getY(), up.getY(), directionY,
                left.getZ(), up.getZ(), directionZ
        );

        this.worldToGlyph.load(this.glyphToWorld);
        this.worldToGlyph.invert();

        Matrix4fAccess.set(this.renderGlyphToWorldMatrix, this.glyphToWorld);
        this.renderGlyphToWorldMatrix.multiply(Matrix4f.scale(1.0F, 1.0F, distance));
    }

    public void setCentered(float x, float y) {
        Vector3f direction = new Vector3f(x, y, 1.0F);
        direction.transform(this.glyphToWorld);
        this.set(direction, this.distance);
    }

    public void projectOntoPlane(Vector3f vector) {
        vector.transform(this.worldToGlyph);

        // once we're in plane space, move it onto the plane by scaling such that z=distance
        vector.scale(this.distance / vector.getZ());
    }

    public Matrix4f getRenderGlyphToWorldMatrix() {
        return this.renderGlyphToWorldMatrix;
    }

    public Vector3f getDirection() {
        return this.direction;
    }

    public float getDistance() {
        return this.distance;
    }
}
