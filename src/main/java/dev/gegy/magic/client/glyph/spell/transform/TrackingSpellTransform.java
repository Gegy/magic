package dev.gegy.magic.client.glyph.spell.transform;

import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.SpellGlyphs;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import org.joml.Vector3f;

public final class TrackingSpellTransform implements SpellTransform {
    private final SpellSource source;

    private final Vector3f direction;
    private final Vector3f prevDirection;

    private final Vector3f resultDirection = new Vector3f();

    private final float castingDistance;

    public TrackingSpellTransform(SpellSource source, float castingDistance) {
        this.source = source;

        this.direction = source.getLookVector(1.0F).toVector3f();
        this.prevDirection = new Vector3f(this.direction);

        this.castingDistance = castingDistance;
    }

    @Override
    public void tick() {
        var target = this.source.getLookVector(1.0F);

        var direction = this.direction;
        this.prevDirection.set(direction);

        direction.add(
                (float) (target.x - direction.x()) * 0.5F,
                (float) (target.y - direction.y()) * 0.5F,
                (float) (target.z - direction.z()) * 0.5F
        );
    }

    @Override
    public Vector3f getDirection(float tickDelta) {
        return this.prevDirection.lerp(this.direction, tickDelta, this.resultDirection);
    }

    @Override
    public float getDistance(final float tickDelta) {
        return this.castingDistance;
    }

    @Override
    public GlyphTransform getTransformForGlyph(int index) {
        float distance = SpellGlyphs.getDistanceForGlyph(index);

        return new GlyphTransform() {
            @Override
            public Vector3f getDirection(float tickDelta) {
                return TrackingSpellTransform.this.getDirection(tickDelta);
            }

            @Override
            public float getDistance(final float tickDelta) {
                return distance;
            }
        };
    }
}
