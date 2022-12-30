package dev.gegy.magic.client.glyph.spell.transform;

import dev.gegy.magic.client.glyph.spell.SpellGlyphs;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import net.minecraft.util.math.Vec3f;

public final class StaticSpellTransform implements SpellTransform {
    private final Vec3f direction;
    private final float distance;

    public StaticSpellTransform(Vec3f direction, float distance) {
        this.direction = direction;
        this.distance = distance;
    }

    @Override
    public Vec3f getDirection(float tickDelta) {
        return this.direction;
    }

    @Override
    public float getDistance(final float tickDelta) {
        return this.distance;
    }

    @Override
    public GlyphTransform getTransformForGlyph(int index) {
        return GlyphTransform.of(this.direction, SpellGlyphs.getDistanceForGlyph(index));
    }
}
