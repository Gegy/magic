package dev.gegy.magic.client.glyph.spell.transform;

import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.SpellGlyphs;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import net.minecraft.util.math.Vec3f;

public final class TrackingSpellTransform implements SpellTransform {
    private final SpellSource source;

    private final Vec3f direction;
    private final Vec3f prevDirection;

    private final Vec3f resultDirection = new Vec3f();

    private final float castingDistance;

    public TrackingSpellTransform(SpellSource source, float castingDistance) {
        this.source = source;

        this.direction = new Vec3f(source.getLookVector(1.0F));
        this.prevDirection = this.direction.copy();

        this.castingDistance = castingDistance;
    }

    @Override
    public void tick() {
        var target = this.source.getLookVector(1.0F);

        var direction = this.direction;
        this.prevDirection.set(direction);

        direction.set(
                direction.getX() + (float) (target.x - direction.getX()) * 0.5F,
                direction.getY() + (float) (target.y - direction.getY()) * 0.5F,
                direction.getZ() + (float) (target.z - direction.getZ()) * 0.5F
        );
    }

    @Override
    public Vec3f getDirection(float tickDelta) {
        var result = this.resultDirection;
        result.set(this.prevDirection);
        result.lerp(this.direction, tickDelta);
        return result;
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
            public Vec3f getDirection(float tickDelta) {
                return TrackingSpellTransform.this.getDirection(tickDelta);
            }

            @Override
            public float getDistance(final float tickDelta) {
                return distance;
            }
        };
    }
}
