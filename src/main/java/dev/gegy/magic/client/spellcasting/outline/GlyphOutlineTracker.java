package dev.gegy.magic.client.spellcasting.outline;

import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class GlyphOutlineTracker {
    private static final float MIN_SEGMENT_LENGTH = 0.01F;
    private static final float MIN_SEGMENT_LENGTH_2 = MIN_SEGMENT_LENGTH * MIN_SEGMENT_LENGTH;

    private static final float LOOP_CLOSE_DISTANCE = 0.1F;
    private static final float LOOP_CLOSE_DISTANCE_2 = LOOP_CLOSE_DISTANCE * LOOP_CLOSE_DISTANCE;

    // TODO: use a circular buffer to avoid shifting array
    private final Vec3f[] samples;

    private final GlyphOutlineSolver solver = new GlyphOutlineSolver();

    private final Vec3f strokeDirection = new Vec3f();
    private final Vec3f segmentDirection = new Vec3f();

    public GlyphOutlineTracker(int sampleBufferSize) {
        this.samples = new Vec3f[sampleBufferSize];
    }

    @Nullable
    public GlyphOutline pushSample(Vec3d look) {
        Vec3f sample = new Vec3f(look);

        Vec3f[] samples = this.samples;
        int sampleBufferSize = samples.length;

        Vec3f lastSample = samples[sampleBufferSize - 1];
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
        Vec3f strokeDirection = null;
        Vec3f segmentDirection = this.segmentDirection;

        for (int i = 0; i < sampleBufferSize - 1; i++) {
            Vec3f start = samples[i];
            Vec3f end = samples[i + 1];

            boolean isSegment = start != null && end != null && start != end;
            if (isSegment && this.canJoinLoopAt(sample, start)) {
                setSegmentDirection(segmentDirection, start, end);

                // lazily compute the stroke direction
                if (strokeDirection == null) {
                    strokeDirection = this.strokeDirection;
                    setSegmentDirection(this.strokeDirection, lastSample, sample);
                }

                // ensure the stroke is going in somewhat similar direction to the compared segment
                if (strokeDirection.dot(segmentDirection) >= 0.0) {
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

    private boolean canJoinLoopAt(Vec3f point, Vec3f to) {
        float deltaX = point.getX() - to.getX();
        float deltaY = point.getY() - to.getY();
        float deltaZ = point.getZ() - to.getZ();
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ < LOOP_CLOSE_DISTANCE_2;
    }

    @Nullable
    private GlyphOutline trySolveOutlineFrom(int fromIdx) {
        Vec3f[] samples = this.samples;
        List<Vec3f> points = new ArrayList<>(samples.length - fromIdx);

        Vec3f lastPoint = null;
        for (int i = fromIdx; i < samples.length; i++) {
            Vec3f point = samples[i];
            if (point != lastPoint) {
                points.add(point);
                lastPoint = point;
            }
        }

        return this.solver.trySolve(points);
    }

    private static void setSegmentDirection(Vec3f result, Vec3f from, Vec3f to) {
        result.set(to.getX(), to.getY(), to.getZ());
        result.subtract(from);
        result.normalize();
    }

    private static boolean isValidSegment(Vec3f from, Vec3f to) {
        float dx = to.getX() - from.getX();
        float dy = to.getY() - from.getY();
        float dz = to.getZ() - from.getZ();
        return dx * dx + dy * dy + dz * dz >= MIN_SEGMENT_LENGTH_2;
    }
}
