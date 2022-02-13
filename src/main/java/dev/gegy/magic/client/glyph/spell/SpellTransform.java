package dev.gegy.magic.client.glyph.spell;

import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import net.minecraft.util.math.Vec3f;

import java.util.List;

public interface SpellTransform extends GlyphTransform {
    static SpellTransform fixed(SpellSource source, List<ClientDrawingGlyph> glyphs) {
        var direction = new Vec3f(source.getLookVector(1.0F));
        float castingDistance = SpellGlyphs.getDistanceForGlyph(glyphs.size());
        return new StaticSpellTransform(direction, castingDistance);
    }

    static SpellTransform tracking(SpellSource source, List<ClientDrawingGlyph> glyphs) {
        float castingDistance = SpellGlyphs.getDistanceForGlyph(glyphs.size());
        return new TrackingSpellTransform(source, castingDistance);
    }

    default void tick() {
    }

    @Override
    Vec3f getOrigin(float tickDelta);

    @Override
    Vec3f getDirection(float tickDelta);

    GlyphTransform getTransformForGlyph(int index);
}
