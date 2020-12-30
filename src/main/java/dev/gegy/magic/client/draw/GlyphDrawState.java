package dev.gegy.magic.client.draw;

import dev.gegy.magic.client.particle.MagicParticles;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public interface GlyphDrawState {
    GlyphDrawState tick(ClientPlayerEntity player);

    final class Idle implements GlyphDrawState {
        private static final int SAMPLE_INTERVAL = 2;
        private static final int SAMPLE_PERIOD = 80;
        private static final int SAMPLE_BUFFER_SIZE = SAMPLE_PERIOD / SAMPLE_INTERVAL;

        private static final float MIN_SEGMENT_LENGTH = 0.01F;
        private static final float MIN_SEGMENT_LENGTH_2 = MIN_SEGMENT_LENGTH * MIN_SEGMENT_LENGTH;

        private static final float LOOP_CLOSE_DISTANCE = 0.1F;
        private static final float LOOP_CLOSE_DISTANCE_2 = LOOP_CLOSE_DISTANCE * LOOP_CLOSE_DISTANCE;

        // TODO: use a circular buffer to avoid shifting array
        private final Vector3f[] samples = new Vector3f[SAMPLE_BUFFER_SIZE];

        private final GlyphOutlineResolver resolver = new GlyphOutlineResolver();

        private final Vector3f strokeDirection = new Vector3f();
        private final Vector3f segmentDirection = new Vector3f();

        @Override
        public GlyphDrawState tick(ClientPlayerEntity player) {
            if (player.age % SAMPLE_INTERVAL == 0) {
                Vec3d look = player.getRotationVec(1.0F);
                Vector3f sample = new Vector3f((float) look.x, (float) look.y, (float) look.z);

                GlyphOutline outline = this.pushSample(sample);
                if (outline != null) {
                    return new DrawInner(outline);
                }
            }

            return this;
        }

        @Nullable
        private GlyphOutline pushSample(Vector3f sample) {
            Vector3f[] samples = this.samples;

            Vector3f lastSample = samples[SAMPLE_BUFFER_SIZE - 1];
            if (lastSample == null) {
                samples[SAMPLE_BUFFER_SIZE - 1] = sample;
                return null;
            }

            // if we haven't moved enough since the last sample, just push the buffer back
            if (!isValidSegment(lastSample, sample)) {
                for (int i = 0; i < SAMPLE_BUFFER_SIZE - 1; i++) {
                    samples[i] = samples[i + 1];
                }
                return null;
            }

            // push the sample buffer back and test for potential loop closes
            Vector3f strokeDirection = null;
            Vector3f segmentDirection = this.segmentDirection;

            for (int i = 0; i < SAMPLE_BUFFER_SIZE - 1; i++) {
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
                        // we have a potential loop close! try resolve an outline from here to the most recent sample
                        GlyphOutline outline = this.tryResolveOutlineFrom(i);
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
            samples[SAMPLE_BUFFER_SIZE - 1] = sample;

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
        private GlyphOutline tryResolveOutlineFrom(int fromIdx) {
            Vector3f[] points = new Vector3f[SAMPLE_BUFFER_SIZE - fromIdx];
            System.arraycopy(this.samples, fromIdx, points, 0, points.length);

            return this.resolver.tryResolve(points);
        }
    }

    final class DrawInner implements GlyphDrawState {
        private final GlyphOutline outline;

        DrawInner(GlyphOutline outline) {
            this.outline = outline;
        }

        @Override
        public GlyphDrawState tick(ClientPlayerEntity player) {
            if (player.isSneaking()) {
                return new Idle();
            }

            if (player.age % 4 != 0) {
                return this;
            }

            GlyphOutline outline = this.outline;

            int count = 20;
            for (int i = 0; i < count; i++) {
                float theta = (float) Math.toRadians(i * 360.0F / count);
                float x = outline.centerX + MathHelper.sin(theta) * outline.radius;
                float y = outline.centerY + MathHelper.cos(theta) * outline.radius;

                Vector3f projectedPoint = new Vector3f(x, y, 1.0F);
                projectedPoint.transform(outline.projectGlyphToWorld);

                Vec3d eyePos = player.getPos().add(0.0, player.getStandingEyeHeight(), 0.0);
                Vec3d pos = eyePos.add(projectedPoint.getX(), projectedPoint.getY(), projectedPoint.getZ());
                player.clientWorld.addParticle(MagicParticles.GLYPH_DRAW_TRAIL, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
            }

            return this;
        }
    }
}
