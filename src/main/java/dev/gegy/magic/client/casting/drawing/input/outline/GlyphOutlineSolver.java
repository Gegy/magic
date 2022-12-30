package dev.gegy.magic.client.casting.drawing.input.outline;

import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GlyphOutlineSolver {
    // radius can deviate by at most 50% of the mean radius
    private static final float RADIUS_DEVIATION_THRESHOLD = 0.5F;

    // we can only have 30% of our points be outliers in terms of radius
    private static final float MAX_RADIUS_DEVIATION_PERCENT = 0.3F;

    // we can only have 10% of our segments be outliers in terms of direction
    private static final float MAX_DIRECTION_DEVIATION_PERCENT = 0.1F;

    // short side of bounds is allowed to be at least 7/10 of long side
    private static final float MIN_SQUARENESS = 0.7F;

    private static final float MIN_RADIUS = 0.125F;

    float centerX;
    float centerY;
    float radius;

    @Nullable
    public GlyphOutline trySolve(List<Vector3f> points) {
        if (points.size() < 3) {
            return null;
        }

        Vector3f forward = this.getForwardVectorFor(points);
        GlyphPlane plane = new GlyphPlane(forward, GlyphTransform.DRAW_DISTANCE);

        Vec2f[] projectedPoints = projectPoints(points, plane);
        if (projectedPoints == null) {
            return null;
        }

        if (this.trySolveFromPoints(projectedPoints)) {
            forward = plane.projectToWorld(this.centerX, this.centerY).normalize();
            plane.set(forward, GlyphTransform.DRAW_DISTANCE);

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

        // validate this circle by counting number of outliers based on segment direction consistency
        if (!this.isDirectionConsistent(points)) {
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
        if (!this.isRadiusConsistent(radii, meanRadius)) {
            return false;
        }

        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = meanRadius;

        return true;
    }

    private boolean isRadiusConsistent(float[] radii, float meanRadius) {
        if (meanRadius < MIN_RADIUS) {
            return false;
        }

        int maxDeviations = MathHelper.ceil(radii.length * MAX_RADIUS_DEVIATION_PERCENT);
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

    private boolean isDirectionConsistent(Vec2f[] points) {
        int segmentCount = points.length - 1;
        int maxDeviations = MathHelper.ceil(segmentCount * MAX_DIRECTION_DEVIATION_PERCENT);

        int deviations = 0;

        Vec2f lastSegmentDirection = null;

        // check that all segments are vaguely going in the same direction
        for (int i = 0; i < points.length; i++) {
            Vec2f start = points[i];
            Vec2f end = points[(i + 1) % points.length];

            Vec2f segmentDirection = getSegmentDirection(start, end);
            if (lastSegmentDirection != null) {
                if (segmentDirection.dot(lastSegmentDirection) < 0.0F) {
                    if (++deviations > maxDeviations) {
                        return false;
                    }
                }
            }

            lastSegmentDirection = segmentDirection;
        }

        return true;
    }

    private static Vec2f getSegmentDirection(Vec2f from, Vec2f to) {
        float directionX = to.x - from.x;
        float directionY = to.y - from.y;

        float normalize = MathHelper.fastInverseSqrt(directionX * directionX + directionY * directionY);
        directionX *= normalize;
        directionY *= normalize;

        return new Vec2f(directionX, directionY);
    }

    @Nullable
    private static Vec2f[] projectPoints(List<Vector3f> points, GlyphPlane plane) {
        Vec2f[] projectedPoints = new Vec2f[points.size()];

        Vector3f projected = new Vector3f();

        for (int i = 0; i < projectedPoints.length; i++) {
            Vector3f point = points.get(i);

            var intersection = plane.raycast(new Vector3f(0.0F, 0.0F, 0.0F), point);
            if (intersection == null) {
                return null;
            }

            plane.projectFromWorld(projected.set(intersection));
            projectedPoints[i] = new Vec2f(projected.x(), projected.y());
        }

        return projectedPoints;
    }

    private Vector3f getForwardVectorFor(List<Vector3f> points) {
        Vector3f forward = new Vector3f();

        float x = 0.0F;
        float y = 0.0F;
        float z = 0.0F;

        float weightPerPoint = 1.0F / points.size();
        for (Vector3f point : points) {
            x += point.x() * weightPerPoint;
            y += point.y() * weightPerPoint;
            z += point.z() * weightPerPoint;
        }

        return forward.set(x, y, z).normalize();
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
