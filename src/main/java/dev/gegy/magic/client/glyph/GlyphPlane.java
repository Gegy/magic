package dev.gegy.magic.client.glyph;

import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import dev.gegy.magic.math.Matrix3fAccess;
import dev.gegy.magic.math.Matrix4fAccess;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public final class GlyphPlane implements GlyphTransform {
    private final Vec3f direction = new Vec3f();
    private float distance;

    private final Matrix3f planeToWorld = new Matrix3f();
    private final Matrix3f worldToPlane = new Matrix3f();

    private final Matrix4f renderPlaneToWorld = new Matrix4f();

    private final Vec3f left = new Vec3f();
    private final Vec3f up = new Vec3f();

    public GlyphPlane() {
    }

    public GlyphPlane(Vec3f direction, float distance) {
        this.set(direction, distance);
    }

    public void set(Vec3f direction, float distance) {
        this.direction.set(direction);
        this.distance = distance;

        var left = this.left;
        left.set(Vec3f.POSITIVE_Y);
        left.cross(direction);
        left.normalize();

        var up = this.up;
        up.set(direction);
        up.cross(left);
        up.normalize();

        Matrix3fAccess.set(this.planeToWorld,
                left.getX(), up.getX(), direction.getX(),
                left.getY(), up.getY(), direction.getY(),
                left.getZ(), up.getZ(), direction.getZ()
        );

        this.worldToPlane.load(this.planeToWorld);
        this.worldToPlane.invert();

        Matrix4fAccess.set(this.renderPlaneToWorld, this.planeToWorld);
    }

    public void set(GlyphTransform transform, float tickDelta) {
        this.set(transform.getDirection(tickDelta), transform.getDistance(tickDelta));
    }

    public void set(GlyphTransform transform) {
        this.set(transform, 1.0F);
    }

    public void centerOn(float x, float y) {
        var direction = new Vec3f(x, y, 1.0F);
        direction.transform(this.planeToWorld);
        this.set(direction, this.distance);
    }

    public void projectOntoPlane(Vec3f vector) {
        vector.transform(this.worldToPlane);

        // once we're in plane space, move it onto the plane by scaling such that z=distance
        vector.scale(this.distance / vector.getZ());
    }

    public void projectFromPlane(Vec3f vector) {
        vector.transform(this.planeToWorld);
    }

    public Vec3f projectFromPlane(float x, float y, float z) {
        Vec3f vector = new Vec3f(x, y, z);
        this.projectFromPlane(vector);
        return vector;
    }

    public Matrix4f getTransformationMatrix() {
        return this.renderPlaneToWorld;
    }

    public Vec3f getDirection() {
        return this.direction;
    }

    public float getDistance() {
        return this.distance;
    }

    @Override
    public Vec3f getDirection(float tickDelta) {
        return this.direction;
    }

    @Override
    public float getDistance(float tickDelta) {
        return this.distance;
    }
}
