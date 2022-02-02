package dev.gegy.magic.client.glyph;

import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import dev.gegy.magic.math.Matrix4fAccess;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import org.jetbrains.annotations.Nullable;

public final class GlyphPlane {
    private final Vec3f origin = new Vec3f();
    private final Vec3f direction = new Vec3f();

    private final Matrix4f lookAt = new Matrix4f();

    private final Matrix4f planeToWorld = new Matrix4f();
    private final Matrix4f worldToPlane = new Matrix4f();

    private final Vec3f left = new Vec3f();
    private final Vec3f up = new Vec3f();

    private final Vec3f vec3 = new Vec3f();
    private final Vector4f vec4 = new Vector4f();

    public GlyphPlane() {
    }

    public GlyphPlane(Vec3f origin, Vec3f direction) {
        this.set(origin, direction);
    }

    public GlyphPlane(Vec3f direction, float distance) {
        this.set(direction, distance);
    }

    public void set(Vec3f direction, float distance) {
        var origin = this.origin;
        origin.set(direction);
        origin.scale(distance);
        this.set(origin, direction);
    }

    public void set(Vec3f origin, Vec3f direction) {
        this.origin.set(origin);
        this.direction.set(direction);

        var lookAt = this.computeLookAtMatrix(direction);

        var planeToWorld = this.planeToWorld;
        planeToWorld.loadIdentity();
        planeToWorld.multiplyByTranslation(origin.getX(), origin.getY(), origin.getZ());
        planeToWorld.multiply(lookAt);

        var worldToPlane = this.worldToPlane;
        worldToPlane.load(planeToWorld);
        worldToPlane.invert();
    }

    private Matrix4f computeLookAtMatrix(Vec3f direction) {
        var left = this.left;
        left.set(Vec3f.POSITIVE_Y);
        left.cross(direction);
        left.normalize();

        var up = this.up;
        up.set(direction);
        up.cross(left);
        up.normalize();

        var lookAt = this.lookAt;
        Matrix4fAccess.setLookAt(lookAt, left, up, direction);

        return lookAt;
    }

    public void set(GlyphTransform transform, float tickDelta) {
        this.set(transform.getOrigin(tickDelta), transform.getDirection(tickDelta));
    }

    public void set(GlyphTransform transform) {
        this.set(transform, 1.0F);
    }

    @Nullable
    public Vec3f raycast(Vec3f origin, Vec3f direction) {
        float denominator = direction.dot(this.direction);
        if (denominator > 1e-3F) {
            var delta = this.vec3;
            delta.set(this.origin);
            delta.subtract(origin);

            float distance = delta.dot(this.direction) / denominator;
            if (distance >= 0.0F) {
                var intersect = this.vec3;
                intersect.set(direction);
                intersect.scale(distance);
                intersect.add(origin);
                return intersect;
            }
        }

        return null;
    }

    public void projectToWorld(Vec3f point) {
        this.transformPoint(point, this.planeToWorld);
    }

    public Vec3f projectToWorld(float x, float y, float z) {
        Vec3f point = new Vec3f(x, y, z);
        this.projectToWorld(point);
        return point;
    }

    public Vec3f projectToWorld(float x, float y) {
        return this.projectToWorld(x, y, 0.0F);
    }

    public void projectFromWorld(Vec3f point) {
        this.transformPoint(point, this.worldToPlane);
    }

    public Vec3f projectFromWorld(float x, float y, float z) {
        Vec3f point = new Vec3f(x, y, z);
        this.projectFromWorld(point);
        return point;
    }

    private void transformPoint(Vec3f point, Matrix4f matrix) {
        var vec4 = this.vec4;
        vec4.set(point.getX(), point.getY(), point.getZ(), 1.0F);
        vec4.transform(matrix);
        point.set(vec4.getX(), vec4.getY(), vec4.getZ());
    }

    public Matrix4f getPlaneToWorldMatrix() {
        return this.planeToWorld;
    }

    public Vec3f getOrigin() {
        return this.origin;
    }

    public Vec3f getDirection() {
        return this.direction;
    }

    public GlyphTransform asTransform() {
        return GlyphTransform.of(this.origin, this.direction);
    }
}
