package dev.gegy.magic.client.glyph.spell.transform;

import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import org.joml.Vector3f;

public interface SpellTransform extends GlyphTransform {
    default void tick() {
    }

    @Override
    Vector3f getDirection(float tickDelta);

    @Override
    float getDistance(float tickDelta);

    GlyphTransform getTransformForGlyph(int index);
}
