package dev.gegy.magic.client.glyph.spellcasting.outline;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class GlyphOutlineTracker {
    private static final float MIN_SEGMENT_LENGTH = 0.01F;
    private static final float MIN_SEGMENT_LENGTH_2 = MIN_SEGMENT_LENGTH * MIN_SEGMENT_LENGTH;

    private static final float LOOP_CLOSE_DISTANCE = 0.1F;
    private static final float LOOP_CLOSE_DISTANCE_2 = LOOP_CLOSE_DISTANCE * LOOP_CLOSE_DISTANCE;

    // TODO: use a circular buffer to avoid shifting array
    private final Vector3f[] samples;

    private final GlyphOutlineSolver solver = new GlyphOutlineSolver();

    private final Vector3f strokeDirection = new Vector3f();
    private final Vector3f segmentDirection = new Vector3f();

    public GlyphOutlineTracker(int sampleBufferSize) {
        this.samples = new Vector3f[sampleBufferSize];
    }

    @Nullable
    public GlyphOutline pushSample(Vec3d look) {
        Vector3f sample = new Vector3f(look);

        Vector3f[] samples = this.samples;
        int sampleBufferSize = samples.length;

        Vector3f lastSample = samples[sampleBufferSize - 1];
        if (lastSample == null) {
            samples[sampleBufferSize - 1] = sample;
            return null;
        }

        // if we haven't moved enough since the last sample, just push the buffer back
        if (!isValidSegment(lastSample, sample)) {
            for (int i = 0; i < sampleBufferSize - 1; i++) {
                samples[i] = samples[i + 1];
            }
            return null;
        }

        // push the sample buffer back and test for potential loop closes
        Vector3f strokeDirection = null;
        Vector3f segmentDirection = this.segmentDirection;

        for (int i = 0; i < sampleBufferSize - 1; i++) {
            Vector3f start = samples[i];
            Vector3f end = samples[i + 1];

            boolean isSegment = start != null && end != null && start != end;
            if (isSegment && this.canJoinLoopAt(sample, start)) {
                segmentDirection.set(end.getX(), end.getY(), end.getZ());
                segmentDirection.subtract(start);
                segmentDirection.normalize();

                // lazily compute the stroke direction
                if (strokeDirection == null) {
                    strokeDirection = this.getStrokeDirection(lastSample, sample);
                }

                // ensure the stroke is going in somewhat similar direction to the compared segment
                if (strokeDirection.dot(segmentDirection) > 0.0) {
                    // we have a potential loop close! try solve an outline from here to the most recent sample
                    GlyphOutline outline = this.trySolveOutlineFrom(i);
                    if (outline != null) {
                        this.clearSampleBuffer();
                        return outline;
                    }
                }
            }

            // push the buffer back
            samples[i] = end;
        }

        // place our new sample into the buffer
        samples[sampleBufferSize - 1] = sample;

        return null;
    }

    private void clearSampleBuffer() {
        Arrays.fill(this.samples, null);
    }

    private boolean canJoinLoopAt(Vector3f point, Vector3f to) {
        float deltaX = point.getX() - to.getX();
        float deltaY = point.getY() - to.getY();
        float deltaZ = point.getZ() - to.getZ();
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ < LOOP_CLOSE_DISTANCE_2;
    }

    private Vector3f getStrokeDirection(Vector3f from, Vector3f to) {
        Vector3f strokeDirection = this.strokeDirection;
        strokeDirection.set(to.getX(), to.getY(), to.getZ());
        strokeDirection.subtract(from);
        strokeDirection.normalize();
        return strokeDirection;
    }

    private static boolean isValidSegment(Vector3f from, Vector3f to) {
        float dx = to.getX() - from.getX();
        float dy = to.getY() - from.getY();
        float dz = to.getZ() - from.getZ();
        return dx * dx + dy * dy + dz * dz >= MIN_SEGMENT_LENGTH_2;
    }

    @Nullable
    private GlyphOutline trySolveOutlineFrom(int fromIdx) {
        Vector3f[] points = new Vector3f[this.samples.length - fromIdx];
        System.arraycopy(this.samples, fromIdx, points, 0, points.length);

        return this.solver.trySolve(points);
    }
}
