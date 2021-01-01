package dev.gegy.magic.client.glyph.draw;

import dev.gegy.magic.client.Matrix3fAccess;
import dev.gegy.magic.client.Matrix4fAccess;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.Nullable;

class GlyphOutlineResolver {
    // radius can deviate by at most 50% of the mean radius
    private static final float RADIUS_DEVIATION_THRESHOLD = 0.5F;

    // we can only have 30% of our points be outliers
    private static final float MAX_DEVIATION_PERCENT = 0.3F;

    // short side of bounds is allowed to be at least 7/10 of long side
    private static final float MIN_SQUARENESS = 0.7F;

    private static final float MIN_RADIUS = 0.125F;

    private final Matrix3f worldToGlyph = new Matrix3f();
    private final Matrix3f glyphToWorld = new Matrix3f();

    private final Vector3f forward = new Vector3f();
    private final Vector3f left = new Vector3f();
    private final Vector3f up = new Vector3f();

    float centerX;
    float centerY;
    float radius;

    @Nullable
    GlyphOutline tryResolve(Vector3f[] points) {
        if (points.length < 3) {
            return null;
        }

        Matrix3f glyphToWorld = this.computeGlyphToWorldProjection(points);

        Matrix3f worldToGlyph = this.worldToGlyph;
        worldToGlyph.load(glyphToWorld);
        worldToGlyph.invert();

        Vector3f[] projectedPoints = projectPoints(points, worldToGlyph);
        if (this.tryResolveFromProjected(projectedPoints)) {
            GlyphPlane plane = this.createGlyphPlane(this.centerX, this.centerY, glyphToWorld);
            return new GlyphOutline(plane, this.radius);
        } else {
            return null;
        }
    }

    private GlyphPlane createGlyphPlane(float centerX, float centerY, Matrix3f glyphToWorld) {
        Matrix4f shiftedGlyphToWorld = Matrix4f.translate(centerX, centerY, 0.0F);
        shiftedGlyphToWorld.multiply(Matrix4fAccess.create(glyphToWorld));

        Matrix4f shiftedWorldToGlyph = shiftedGlyphToWorld.copy();
        shiftedWorldToGlyph.invert();

        return new GlyphPlane(shiftedGlyphToWorld, shiftedWorldToGlyph);
    }

    private boolean tryResolveFromProjected(Vector3f[] points) {
        // compute encompassing bounds of these points
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (Vector3f point : points) {
            float x = point.getX();
            float y = point.getY();
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
            Vector3f point = points[i];
            float dx = point.getX() - centerX;
            float dy = point.getY() - centerY;

            float radius = (float) Math.sqrt(dx * dx + dy * dy);

            radii[i] = radius;
            meanRadius += radius * meanRadiusWeight;
        }

        // validate this circle by counting the number of outliers based on radius
        if (!this.isValidCircle(points, radii, meanRadius)) {
            return false;
        }

        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = meanRadius;

        return true;
    }

    private boolean isValidCircle(Vector3f[] points, float[] radii, float meanRadius) {
        if (meanRadius < MIN_RADIUS) {
            return false;
        }

        int maxDeviations = MathHelper.ceil(points.length * MAX_DEVIATION_PERCENT);
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

    private static Vector3f[] projectPoints(Vector3f[] points, Matrix3f projection) {
        Vector3f[] projectedPoints = new Vector3f[points.length];

        for (int i = 0; i < projectedPoints.length; i++) {
            Vector3f projectedPoint = points[i].copy();
            projectedPoint.transform(projection);
            projectedPoints[i] = projectedPoint;
        }

        return projectedPoints;
    }

    private Matrix3f computeGlyphToWorldProjection(Vector3f[] points) {
        Vector3f forward = this.getForwardVectorFor(points);

        Vector3f left = this.left;
        left.set(0.0F, 1.0F, 0.0F);
        left.cross(forward);
        left.normalize();

        Vector3f up = this.up;
        up.set(forward.getX(), forward.getY(), forward.getZ());
        up.cross(left);
        up.normalize();

        Matrix3f glyphToWorld = this.glyphToWorld;

        Matrix3fAccess.set(glyphToWorld,
                left.getX(), up.getX(), forward.getX(),
                left.getY(), up.getY(), forward.getY(),
                left.getZ(), up.getZ(), forward.getZ()
        );

        return glyphToWorld;
    }

    private Vector3f getForwardVectorFor(Vector3f[] points) {
        Vector3f forward = this.forward;

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
