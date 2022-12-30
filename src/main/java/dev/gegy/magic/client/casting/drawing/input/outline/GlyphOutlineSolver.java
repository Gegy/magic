package dev.gegy.magic.client.casting.drawing.input.outline;

import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class GlyphOutlineSolver {
    // radius can deviate by at most 50% of the mean radius
    private static final float RADIUS_DEVIATION_THRESHOLD = 0.5f;

    // we can only have 30% of our points be outliers in terms of radius
    private static final float MAX_RADIUS_DEVIATION_PERCENT = 0.3F;

    // we can only have 10% of our segments be outliers in terms of direction
    private static final float MAX_DIRECTION_DEVIATION_PERCENT = 0.1F;

    // short side of bounds is allowed to be at least 7/10 of long side
    private static final float MIN_SQUARENESS = 0.7F;

    private static final float MIN_RADIUS = 0.125f;

    float centerX;
    float centerY;
    float radius;

    @Nullable
    public GlyphOutline trySolve(final List<Vector3f> points) {
        if (points.size() < 3) {
            return null;
        }

        Vector3f forward = getForwardVectorFor(points);
        final GlyphPlane plane = new GlyphPlane(forward, GlyphTransform.DRAW_DISTANCE);

        final Vec2[] projectedPoints = projectPoints(points, plane);
        if (projectedPoints == null) {
            return null;
        }

        if (trySolveFromPoints(projectedPoints)) {
            forward = plane.projectToWorld(centerX, centerY).normalize();
            plane.set(forward, GlyphTransform.DRAW_DISTANCE);

            return new GlyphOutline(plane, radius);
        } else {
            return null;
        }
    }

    private boolean trySolveFromPoints(final Vec2[] points) {
        // compute encompassing bounds of these points
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (final Vec2 point : points) {
            final float x = point.x;
            final float y = point.y;
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }

        // this is obviously not a circle if the player drew a rectangle
        if (!isValidSquare(minX, minY, maxX, maxY)) {
            return false;
        }

        // validate this circle by counting number of outliers based on segment direction consistency
        if (!isDirectionConsistent(points)) {
            return false;
        }

        // find center based on center of bounding box
        final float centerX = (maxX + minX) / 2.0f;
        final float centerY = (maxY + minY) / 2.0f;

        final float[] radii = new float[points.length];

        float meanRadius = 0.0f;
        final float meanRadiusWeight = 1.0f / radii.length;

        // compute apparent radii from each point and find mean
        for (int i = 0; i < points.length; i++) {
            final Vec2 point = points[i];
            final float dx = point.x - centerX;
            final float dy = point.y - centerY;

            final float radius = (float) Math.sqrt(dx * dx + dy * dy);

            radii[i] = radius;
            meanRadius += radius * meanRadiusWeight;
        }

        // validate this circle by counting the number of outliers based on radius
        if (!isRadiusConsistent(radii, meanRadius)) {
            return false;
        }

        this.centerX = centerX;
        this.centerY = centerY;
        radius = meanRadius;

        return true;
    }

    private boolean isRadiusConsistent(final float[] radii, final float meanRadius) {
        if (meanRadius < MIN_RADIUS) {
            return false;
        }

        final int maxDeviations = Mth.ceil(radii.length * MAX_RADIUS_DEVIATION_PERCENT);
        final float radiusDeviationThreshold = meanRadius * RADIUS_DEVIATION_THRESHOLD;

        int deviations = 0;

        for (final float radius : radii) {
            final float deltaRadius = Math.abs(radius - meanRadius);
            if (deltaRadius > radiusDeviationThreshold) {
                if (++deviations > maxDeviations) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isDirectionConsistent(final Vec2[] points) {
        final int segmentCount = points.length - 1;
        final int maxDeviations = Mth.ceil(segmentCount * MAX_DIRECTION_DEVIATION_PERCENT);

        int deviations = 0;

        Vec2 lastSegmentDirection = null;

        // check that all segments are vaguely going in the same direction
        for (int i = 0; i < points.length; i++) {
            final Vec2 start = points[i];
            final Vec2 end = points[(i + 1) % points.length];

            final Vec2 segmentDirection = getSegmentDirection(start, end);
            if (lastSegmentDirection != null) {
                if (segmentDirection.dot(lastSegmentDirection) < 0.0f) {
                    if (++deviations > maxDeviations) {
                        return false;
                    }
                }
            }

            lastSegmentDirection = segmentDirection;
        }

        return true;
    }

    private static Vec2 getSegmentDirection(final Vec2 from, final Vec2 to) {
        float directionX = to.x - from.x;
        float directionY = to.y - from.y;

        final float normalize = Mth.fastInvSqrt(directionX * directionX + directionY * directionY);
        directionX *= normalize;
        directionY *= normalize;

        return new Vec2(directionX, directionY);
    }

    @Nullable
    private static Vec2[] projectPoints(final List<Vector3f> points, final GlyphPlane plane) {
        final Vec2[] projectedPoints = new Vec2[points.size()];

        final Vector3f projected = new Vector3f();

        for (int i = 0; i < projectedPoints.length; i++) {
            final Vector3f point = points.get(i);

            final Vector3f intersection = plane.raycast(new Vector3f(0.0f, 0.0f, 0.0f), point);
            if (intersection == null) {
                return null;
            }

            plane.projectFromWorld(projected.set(intersection));
            projectedPoints[i] = new Vec2(projected.x(), projected.y());
        }

        return projectedPoints;
    }

    private Vector3f getForwardVectorFor(final List<Vector3f> points) {
        final Vector3f forward = new Vector3f();

        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;

        final float weightPerPoint = 1.0f / points.size();
        for (final Vector3f point : points) {
            x += point.x() * weightPerPoint;
            y += point.y() * weightPerPoint;
            z += point.z() * weightPerPoint;
        }

        return forward.set(x, y, z).normalize();
    }

    private boolean isValidSquare(final float minX, final float minY, final float maxX, final float maxY) {
        final float sizeX = maxX - minX;
        final float sizeY = maxY - minY;

        final float longSide = Math.max(sizeX, sizeY);
        final float shortSide = Math.min(sizeX, sizeY);
        final float squareness = shortSide / longSide;

        return squareness >= MIN_SQUARENESS;
    }
}
