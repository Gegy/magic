package dev.gegy.magic.client.glyph.spellcasting.outline;

import dev.gegy.magic.client.glyph.plane.GlyphPlane;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

public class GlyphOutlineSolver {
    // radius can deviate by at most 50% of the mean radius
    private static final float RADIUS_DEVIATION_THRESHOLD = 0.5F;

    // we can only have 30% of our points be outliers
    private static final float MAX_DEVIATION_PERCENT = 0.3F;

    // short side of bounds is allowed to be at least 7/10 of long side
    private static final float MIN_SQUARENESS = 0.7F;

    private static final float MIN_RADIUS = 0.125F;

    float centerX;
    float centerY;
    float radius;

    @Nullable
    public GlyphOutline trySolve(Vector3f[] points) {
        if (points.length < 3) {
            return null;
        }

        Vector3f forward = this.getForwardVectorFor(points);
        GlyphPlane plane = GlyphPlane.create(forward, GlyphPlane.DRAW_DISTANCE);

        Vec2f[] projectedPoints = projectPoints(points, plane);
        if (this.trySolveFromPoints(projectedPoints)) {
            plane = plane.centered(this.centerX, this.centerY);
            return new GlyphOutline(plane, this.radius);
        } else {
            return null;
        }
    }

    private boolean trySolveFromPoints(Vec2f[] points) {
        // compute encompassing bounds of these points
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (Vec2f point : points) {
            float x = point.x;
            float y = point.y;
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }

        // this is obviously not a circle if the player drew a rectangle
        if (!this.isValidSquare(minX, minY, maxX, maxY)) {
            return false;
        }

        // find center based on center of bounding box
        float centerX = (maxX + minX) / 2.0F;
        float centerY = (maxY + minY) / 2.0F;

        float[] radii = new float[points.length];

        float meanRadius = 0.0F;
        float meanRadiusWeight = 1.0F / radii.length;

        // compute apparent radii from each point and find mean
        for (int i = 0; i < points.length; i++) {
            Vec2f point = points[i];
            float dx = point.x - centerX;
            float dy = point.y - centerY;

            float radius = (float) Math.sqrt(dx * dx + dy * dy);

            radii[i] = radius;
            meanRadius += radius * meanRadiusWeight;
        }

        // validate this circle by counting the number of outliers based on radius
        if (!this.isValidCircle(radii, meanRadius)) {
            return false;
        }

        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = meanRadius;

        return true;
    }

    private boolean isValidCircle(float[] radii, float meanRadius) {
        if (meanRadius < MIN_RADIUS) {
            return false;
        }

        int maxDeviations = MathHelper.ceil(radii.length * MAX_DEVIATION_PERCENT);
        float radiusDeviationThreshold = meanRadius * RADIUS_DEVIATION_THRESHOLD;

        int deviations = 0;

        for (float radius : radii) {
            float deltaRadius = Math.abs(radius - meanRadius);
            if (deltaRadius > radiusDeviationThreshold) {
                if (++deviations > maxDeviations) {
                    return false;
                }
            }
        }

        return true;
    }

    private static Vec2f[] projectPoints(Vector3f[] points, GlyphPlane plane) {
        Vec2f[] projectedPoints = new Vec2f[points.length];

        Vector3f projected = new Vector3f();

        for (int i = 0; i < projectedPoints.length; i++) {
            Vector3f point = points[i];

            projected.set(point.getX(), point.getY(), point.getZ());
            plane.projectOntoPlane(projected, 1.0F);

            projectedPoints[i] = new Vec2f(projected.getX(), projected.getY());
        }

        return projectedPoints;
    }

    private Vector3f getForwardVectorFor(Vector3f[] points) {
        Vector3f forward = new Vector3f();

        float x = 0.0F;
        float y = 0.0F;
        float z = 0.0F;

        float weightPerPoint = 1.0F / points.length;
        for (Vector3f point : points) {
            x += point.getX() * weightPerPoint;
            y += point.getY() * weightPerPoint;
            z += point.getZ() * weightPerPoint;
        }

        forward.set(x, y, z);
        forward.normalize();

        return forward;
    }

    private boolean isValidSquare(float minX, float minY, float maxX, float maxY) {
        float sizeX = maxX - minX;
        float sizeY = maxY - minY;

        float longSide = Math.max(sizeX, sizeY);
        float shortSide = Math.min(sizeX, sizeY);
        float squareness = shortSide / longSide;

        return squareness >= MIN_SQUARENESS;
    }
}
