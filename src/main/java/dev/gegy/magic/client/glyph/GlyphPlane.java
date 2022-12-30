package dev.gegy.magic.client.glyph;

import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class GlyphPlane {
    private static final Vector3f UP = new Vector3f(0.0F, 1.0F, 0.0F);

    private final Vector3f origin = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private float distance;

    private final Matrix4f planeToWorld = new Matrix4f();
    private final Matrix4f worldToPlane = new Matrix4f();

    private final Vector3f vec3 = new Vector3f();

    public GlyphPlane() {
    }

    public GlyphPlane(Vector3f direction, float distance) {
        this.set(direction, distance);
    }

    public void set(Vector3f direction, float distance) {
        this.origin.set(direction).mul(distance);
        this.direction.set(direction);
        this.distance = distance;

        this.planeToWorld.rotationTowards(direction, UP).translate(0.0F, 0.0F, distance);
        this.worldToPlane.set(this.planeToWorld).invert();
    }

    public void set(GlyphTransform transform, float tickDelta) {
        this.set(transform.getDirection(tickDelta), transform.getDistance(tickDelta));
    }

    public void set(GlyphTransform transform) {
        this.set(transform, 1.0F);
    }

    @Nullable
    public Vector3f raycast(Vector3f origin, Vector3f direction) {
        float denominator = direction.dot(this.direction);
        if (denominator > 1e-3F) {
            var delta = this.origin.sub(origin, this.vec3);

            float distance = delta.dot(this.direction) / denominator;
            if (distance >= 0.0F) {
                return direction.mul(distance, this.vec3).add(origin);
            }
        }

        return null;
    }

    public Vector3f projectToWorld(Vector3f point) {
        return point.mulPosition(this.planeToWorld);
    }

    public Vector3f projectToWorld(float x, float y, float z) {
        return this.projectToWorld(new Vector3f(x, y, z));
    }

    public Vector3f projectToWorld(float x, float y) {
        return this.projectToWorld(x, y, 0.0F);
    }

    public Vector3f projectFromWorld(Vector3f point) {
        return point.mulPosition(this.worldToPlane);
    }

    public Matrix4f planeToWorld() {
        return this.planeToWorld;
    }

    public Vector3f origin() {
        return this.origin;
    }

    public Vector3f direction() {
        return this.direction;
    }

    public float distance() {
        return this.distance;
    }

    public GlyphTransform asTransform() {
        return GlyphTransform.of(this.direction, this.distance);
    }
}
