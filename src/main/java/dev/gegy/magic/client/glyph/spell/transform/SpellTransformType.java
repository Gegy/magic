package dev.gegy.magic.client.glyph.spell.transform;

import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.SpellGlyphs;
import org.joml.Vector3f;

public interface SpellTransformType {
    SpellTransformType FIXED = (source, direction, glyphCount) -> {
        float castingDistance = SpellGlyphs.getDistanceForGlyph(glyphCount);
        return new StaticSpellTransform(direction, castingDistance);
    };

    SpellTransformType TRACKING = (source, direction, glyphCount) -> {
        float castingDistance = SpellGlyphs.getDistanceForGlyph(glyphCount);
        return new TrackingSpellTransform(source, castingDistance);
    };

    SpellTransform create(SpellSource source, Vector3f direction, int glyphCount);
}
