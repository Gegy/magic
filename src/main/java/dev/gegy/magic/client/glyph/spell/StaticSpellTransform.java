package dev.gegy.magic.client.glyph.spell;

import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import net.minecraft.util.math.Vec3f;

public final class StaticSpellTransform implements SpellTransform {
    private final Vec3f origin;
    private final Vec3f direction;

    public StaticSpellTransform(Vec3f direction, float castingDistance) {
        this.origin = direction.copy();
        this.origin.scale(castingDistance);
        this.direction = direction;
    }

    @Override
    public Vec3f getOrigin(float tickDelta) {
        return this.origin;
    }

    @Override
    public Vec3f getDirection(float tickDelta) {
        return this.direction;
    }

    @Override
    public GlyphTransform getTransformForGlyph(int index) {
        var origin = this.direction.copy();
        origin.scale(SpellGlyphs.getDistanceForGlyph(index));

        return GlyphTransform.of(origin, this.direction);
    }
}
