package dev.gegy.magic.client.glyph.spell.transform;

import dev.gegy.magic.client.glyph.spell.SpellGlyphs;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import org.joml.Vector3f;

public final class StaticSpellTransform implements SpellTransform {
    private final Vector3f direction;
    private final float distance;

    public StaticSpellTransform(final Vector3f direction, final float distance) {
        this.direction = direction;
        this.distance = distance;
    }

    @Override
    public Vector3f getDirection(final float tickDelta) {
        return direction;
    }

    @Override
    public float getDistance(final float tickDelta) {
        return distance;
    }

    @Override
    public GlyphTransform getTransformForGlyph(final int index) {
        return GlyphTransform.of(direction, SpellGlyphs.getDistanceForGlyph(index));
    }
}
