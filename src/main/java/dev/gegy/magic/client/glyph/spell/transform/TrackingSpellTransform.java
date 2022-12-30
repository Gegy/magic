package dev.gegy.magic.client.glyph.spell.transform;

import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.SpellGlyphs;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class TrackingSpellTransform implements SpellTransform {
    private final SpellSource source;

    private final Vector3f direction;
    private final Vector3f prevDirection;

    private final Vector3f resultDirection = new Vector3f();

    private final float castingDistance;

    public TrackingSpellTransform(final SpellSource source, final float castingDistance) {
        this.source = source;

        direction = source.getLookVector(1.0f).toVector3f();
        prevDirection = new Vector3f(direction);

        this.castingDistance = castingDistance;
    }

    @Override
    public void tick() {
        final Vec3 target = source.getLookVector(1.0f);

        final Vector3f direction = this.direction;
        prevDirection.set(direction);

        direction.add(
                (float) (target.x - direction.x()) * 0.5f,
                (float) (target.y - direction.y()) * 0.5f,
                (float) (target.z - direction.z()) * 0.5f
        );
    }

    @Override
    public Vector3f getDirection(final float tickDelta) {
        return prevDirection.lerp(direction, tickDelta, resultDirection);
    }

    @Override
    public float getDistance(final float tickDelta) {
        return castingDistance;
    }

    @Override
    public GlyphTransform getTransformForGlyph(final int index) {
        final float distance = SpellGlyphs.getDistanceForGlyph(index);

        return new GlyphTransform() {
            @Override
            public Vector3f getDirection(final float tickDelta) {
                return TrackingSpellTransform.this.getDirection(tickDelta);
            }

            @Override
            public float getDistance(final float tickDelta) {
                return distance;
            }
        };
    }
}
