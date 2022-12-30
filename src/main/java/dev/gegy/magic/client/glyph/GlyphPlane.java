package dev.gegy.magic.client.glyph;

import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class GlyphPlane {
    private static final Vector3f UP = new Vector3f(0.0f, 1.0f, 0.0f);

    private final Vector3f origin = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private float distance;

    private final Matrix4f planeToWorld = new Matrix4f();
    private final Matrix4f worldToPlane = new Matrix4f();

    private final Vector3f vec3 = new Vector3f();

    public GlyphPlane() {
    }

    public GlyphPlane(final Vector3f direction, final float distance) {
        set(direction, distance);
    }

    public void set(final Vector3f direction, final float distance) {
        origin.set(direction).mul(distance);
        this.direction.set(direction);
        this.distance = distance;

        planeToWorld.rotationTowards(direction, UP).translate(0.0f, 0.0f, distance);
        worldToPlane.set(planeToWorld).invert();
    }

    public void set(final GlyphTransform transform, final float tickDelta) {
        set(transform.getDirection(tickDelta), transform.getDistance(tickDelta));
    }

    public void set(final GlyphTransform transform) {
        set(transform, 1.0f);
    }

    @Nullable
    public Vector3f raycast(final Vector3f origin, final Vector3f direction) {
        final float denominator = direction.dot(this.direction);
        if (denominator > 1e-3f) {
            final Vector3f delta = this.origin.sub(origin, vec3);

            final float distance = delta.dot(this.direction) / denominator;
            if (distance >= 0.0f) {
                return direction.mul(distance, vec3).add(origin);
            }
        }

        return null;
    }

    public Vector3f projectToWorld(final Vector3f point) {
        return point.mulPosition(planeToWorld);
    }

    public Vector3f projectToWorld(final float x, final float y, final float z) {
        return projectToWorld(new Vector3f(x, y, z));
    }

    public Vector3f projectToWorld(final float x, final float y) {
        return projectToWorld(x, y, 0.0f);
    }

    public Vector3f projectFromWorld(final Vector3f point) {
        return point.mulPosition(worldToPlane);
    }

    public Matrix4f planeToWorld() {
        return planeToWorld;
    }

    public Vector3f origin() {
        return origin;
    }

    public Vector3f direction() {
        return direction;
    }

    public float distance() {
        return distance;
    }

    public GlyphTransform asTransform() {
        return GlyphTransform.of(direction, distance);
    }
}
