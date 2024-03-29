package dev.gegy.magic.client.casting.drawing.input.outline;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public GlyphOutlineTracker(final int sampleBufferSize) {
        samples = new Vector3f[sampleBufferSize];
    }

    @Nullable
    public GlyphOutline pushSample(final Vec3 look) {
        final Vector3f sample = look.toVector3f();

        final Vector3f[] samples = this.samples;
        final int sampleBufferSize = samples.length;

        final Vector3f lastSample = samples[sampleBufferSize - 1];
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
        final Vector3f segmentDirection = this.segmentDirection;

        for (int i = 0; i < sampleBufferSize - 1; i++) {
            final Vector3f start = samples[i];
            final Vector3f end = samples[i + 1];

            final boolean isSegment = start != null && end != null && start != end;
            if (isSegment && canJoinLoopAt(sample, start)) {
                setSegmentDirection(segmentDirection, start, end);

                // lazily compute the stroke direction
                if (strokeDirection == null) {
                    strokeDirection = this.strokeDirection;
                    setSegmentDirection(this.strokeDirection, lastSample, sample);
                }

                // ensure the stroke is going in somewhat similar direction to the compared segment
                if (strokeDirection.dot(segmentDirection) >= 0.0) {
                    // we have a potential loop close! try solve an outline from here to the most recent sample
                    final GlyphOutline outline = trySolveOutlineFrom(i);
                    if (outline != null) {
                        clearSampleBuffer();
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
        Arrays.fill(samples, null);
    }

    private boolean canJoinLoopAt(final Vector3f point, final Vector3f to) {
        return point.distanceSquared(to) < LOOP_CLOSE_DISTANCE_2;
    }

    @Nullable
    private GlyphOutline trySolveOutlineFrom(final int fromIdx) {
        final Vector3f[] samples = this.samples;
        final List<Vector3f> points = new ArrayList<>(samples.length - fromIdx);

        Vector3f lastPoint = null;
        for (int i = fromIdx; i < samples.length; i++) {
            final Vector3f point = samples[i];
            if (point != lastPoint) {
                points.add(point);
                lastPoint = point;
            }
        }

        return solver.trySolve(points);
    }

    private static void setSegmentDirection(final Vector3f result, final Vector3f from, final Vector3f to) {
        to.sub(from, result).normalize();
    }

    private static boolean isValidSegment(final Vector3f from, final Vector3f to) {
        return to.distanceSquared(from) >= MIN_SEGMENT_LENGTH_2;
    }
}
